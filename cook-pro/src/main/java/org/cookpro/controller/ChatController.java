package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.cookpro.AgentBackground;
import org.cookpro.R;
import org.cookpro.config.factory.AgentModelFactory;
import org.cookpro.dto.ChatRecordAddDTO;
import org.cookpro.dto.UserChattingDTO;
import org.cookpro.entity.ChatRecord;
import org.cookpro.dto.ToolChatDTO;
import org.cookpro.enums.HITLStatusEnum;
import org.cookpro.exception.ChatException;
import org.cookpro.service.*;
import org.cookpro.utils.HITLHelper;
import org.cookpro.config.factory.ToolFactory;
import org.cookpro.utils.SystemPrinter;
import org.cookpro.utils.ToolUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口")
public class ChatController {

    @Resource
    ToolService toolService;
    @Resource
    HITLService hitlService;
    @Resource
    ToolFactory toolFactory;

    @Resource
    RAGService ragService;

    //    @Resource
//    FluxResponseService fluxResponseService;
    @Resource
    MemoryCacheService memoryCacheService;

    @Resource
    ChatRecordService chatRecordService;

    @Resource
    UserPreferenceService userPreferenceService;
    private final DashScopeChatModel chatModel;// 构造函数中初始化
    private static final AgentBackground BACKGROUND = AgentBackground.COOKING_ASSISTANT;

    @GetMapping("/chat")
    @Operation(summary = "与烹饪助手聊天", description = "向烹饪助手发送消息，获取回复")
    public R<String> chat(@RequestParam("message") String message,
                          @RequestParam(value = "userId", required = false, defaultValue = "0") Long userId) {

        ReactAgent agent = ReactAgent.builder()
                .name(BACKGROUND.name())
                .model(chatModel)
                .outputType(String.class)
                .systemPrompt(BACKGROUND.systemPrompt)
                .build();

        try {
            AssistantMessage call = agent.call(message);

            // 保存聊天记录
            ChatRecord record = new ChatRecord();

            record.setUserId(userId);

            record.setUserMessage(message);
            record.setBotResponse(call.getText());

            chatRecordService.save(record);
            return R.ok(call.getText());
        } catch (GraphRunnerException e) {
            throw new ChatException("聊天失败: " + e.getMessage(), e);
        }
    }

    @PostMapping("/chatMore")
    @Operation(summary = "与烹饪助手进行功能更多的聊天", description = "向烹饪助手发送消息列表，获取回复")
    public R<String> chat(@RequestBody UserChattingDTO dto) throws GraphRunnerException, IOException, InterruptedException {

        String message = dto.getMessage();


        List<ToolChatDTO> toolDtos = dto.getToolList();
        // 根据用户的选项，构建对应的 Hook 列表，并将其添加到 Agent 中

        List<Hook> hookList = new LinkedList<>();
        if (dto.getHitlEnabled()) {
            hookList.add(HITLHelper.buildHITLHook(toolDtos));
        }
        if (dto.getUseRAG()) {
            hookList.add(ragService.getRAGMessagesHook());
        }

        ReactAgent agent = ReactAgent.builder()
                .name(BACKGROUND.name())
                .model(chatModel)
                .outputType(String.class)
                .hooks(hookList)
                .systemPrompt(BACKGROUND.systemPrompt)
                .tools(toolService.selectTools(toolDtos))
                .saver(new MemorySaver())
                .build();


        Long userId = dto.getUserId();


        String agentThreadId = "user-session-" + userId;
        RunnableConfig config = RunnableConfig.builder()
                .threadId(agentThreadId)
                .build();


        Optional<NodeOutput> result = agent.invokeAndGetOutput(message, config);

        if (result.isPresent()) {
            NodeOutput output = result.get();


            ChatRecordAddDTO recordAddDTO = new ChatRecordAddDTO();
            if (output instanceof InterruptionMetadata interruptionMetadata) {
               hitlService.initHITL(toolDtos, userId, agentThreadId, interruptionMetadata);
                //发生中断  设置状态为 待审核
                recordAddDTO.setHitlStatus(HITLStatusEnum.WAITING.description);
            } else {
                // 没有发生中断，设置状态为 无需审核
                recordAddDTO.setHitlStatus(HITLStatusEnum.NON_AUDIT.description);
            }
            AssistantMessage response = HITLHelper.getAssistantResponse(result.get().state());
            // 保存聊天历史


            recordAddDTO.setUserMessage(message);
            recordAddDTO.setBotResponse(response.getText());
            recordAddDTO.setUserId(userId);
            recordAddDTO.setToolCalls(ToolUtils.convertToolCall(response.getToolCalls()));
            recordAddDTO.setThreadId(agentThreadId);

            chatRecordService.saveOneRecord(recordAddDTO);

            return R.ok(response.getText());
        } else {
            throw new ChatException("聊天失败: 未获取到回复");
        }
    }

    @PostMapping("/stream/chatMore")
    @Operation(summary = "与烹饪助手进行功能更多的聊天（流式）", description = "向烹饪助手发送消息列表，获取回复（流式）")
    public Flux<String> streamChat(@RequestBody UserChattingDTO dto) throws GraphRunnerException, IOException, InterruptedException {
        String message = dto.getMessage();


        AgentBackground cookingAssistant = AgentBackground.COOKING_ASSISTANT;

        List<ToolChatDTO> toolDtos = dto.getToolList();
        // 根据用户的选项，构建对应的 Hook 列表，并将其添加到 Agent 中

        List<Hook> hookList = new LinkedList<>();
        if (dto.getHitlEnabled()) {
            hookList.add(HITLHelper.buildHITLHook(toolDtos));
        }
        if (dto.getUseRAG()) {
            hookList.add(ragService.getRAGMessagesHook());
        }

        ReactAgent agent = ReactAgent.builder()
                .name(cookingAssistant.name())
                .model(chatModel)
                .outputType(String.class)
                .hooks(hookList)
                .systemPrompt(cookingAssistant.systemPrompt)
                .tools(toolService.selectTools(toolDtos))
                .saver(new MemorySaver())
                .build();


        Long userId = dto.getUserId();

        // 构建并发送请求
        String agentThreadId = "user-agent-" + userId + UUID.randomUUID();
        RunnableConfig config = RunnableConfig.builder()
                .threadId(agentThreadId)
                .build();
        Flux<NodeOutput> baseFluxResult = agent.stream(message, config);


        // 创建 并保存 sink
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        memoryCacheService.cacheInterruptSink(agentThreadId, sink);

        baseFluxResult.subscribe(output -> {

            if (output instanceof InterruptionMetadata interruptionMetadata) {
                // 初始化 中断信息
                hitlService.initHITL(toolDtos,userId,agentThreadId,interruptionMetadata);

                if (memoryCacheService.hasInterruptSink(agentThreadId)) {
                    sink.tryEmitNext("[系统] 发生了工具调用中断，请等待人工审核结果...");
                } else {
                    sink.tryEmitNext("[系统] 系统出现异常，无法处理人工审核，请稍后再试...");
                    sink.tryEmitComplete();
                }
            }
            if (output instanceof StreamingOutput<?> streamingOutput) {
                // 处理流式数据
                String chunk = streamingOutput.chunk();

                if (chunk != null && !chunk.isEmpty()) {

                    SystemPrinter.println("流式chunk: " + chunk);

                    sink.tryEmitNext(chunk);
                }
            }

        });

        return sink.asFlux();
    }



    public ChatController(AgentModelFactory factory) {
        Optional<ChatModel> agentModel = factory.getAgentModel(BACKGROUND);
        if (agentModel.isPresent()) {
            this.chatModel = (DashScopeChatModel) agentModel.get();
        } else {
            throw new IllegalStateException("未找到烹饪助手的聊天模型");
        }
    }
}
