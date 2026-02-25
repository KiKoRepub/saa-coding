package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.cookpro.AgentBackground;
import org.cookpro.R;
import org.cookpro.dto.UserChattingDTO;
import org.cookpro.entity.HITLEntity;
import org.cookpro.entity.ToolEntity;
import org.cookpro.enums.SSEEventEnum;
import org.cookpro.exception.ChatException;
import org.cookpro.service.HITLService;
import org.cookpro.service.RAGService;
import org.cookpro.service.SSEService;
import org.cookpro.service.ToolService;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.ToolFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口")
public class ChatController {


    @Resource
    DashScopeChatModel chatModel;

    @Resource
    ToolService toolService;

    @Resource
    HITLService hitlService;

    @Resource
    ToolFactory toolFactory;

    @Resource
    SSEService sseService;

    @Resource
    RAGService ragService;



    @GetMapping("/chat")
    @Operation(summary = "与烹饪助手聊天", description = "向烹饪助手发送消息，获取回复")
    public R<String> chat(@RequestParam("message") String message){

        AgentBackground cookingAssistant = AgentBackground.COOKING_ASSISTANT;

        ReactAgent agent = ReactAgent.builder()
                .name(cookingAssistant.name())
                .model(chatModel)
                .outputType(String.class)
                .systemPrompt(cookingAssistant.systemPrompt)
                .build();

        try {
            AssistantMessage call = agent.call(message);

            return R.ok(call.getText());
        }catch (GraphRunnerException e){
            throw  new ChatException("聊天失败: " + e.getMessage(), e);
        }
    }

    @PostMapping("/chatMore")
    @Operation(summary = "与烹饪助手进行功能更多的聊天", description = "向烹饪助手发送消息列表，获取回复")
    public R<String> chat(@RequestBody UserChattingDTO dto) throws GraphRunnerException, IOException, InterruptedException {

        String message = dto.getMessage();


        AgentBackground cookingAssistant = AgentBackground.COOKING_ASSISTANT;

        List<ToolEntity> toolEntities = toolService.getToolEntities(dto.getToolIdList());
        // 根据用户的选项，构建对应的 Hook 列表，并将其添加到 Agent 中

        List<Hook> hookList = new LinkedList<>();
        if (dto.getHitlEnabled()) {
            hookList.add(HITLHelper.buildHITLHook(toolEntities));
        }
        if(dto.getUseRAG()){
            hookList.add(ragService.getRAGMessagesHook());
        }

        ReactAgent agent = ReactAgent.builder()
                .name(cookingAssistant.name())
                .model(chatModel)
                .outputType(String.class)
                .hooks(hookList)
                .systemPrompt(cookingAssistant.systemPrompt)
                .tools(toolFactory.selectTools(toolEntities))
                .saver(new MemorySaver())
                .build();


        Long userId = dto.getUserId();


        String agentThreadId = "user-session-" + userId;
        RunnableConfig config = RunnableConfig.builder()
                .threadId(agentThreadId)
                .build();

/*
        SseEmitter emitter = new SseEmitter();

        try {
            Flux<NodeOutput> flux = agent.stream(message, config);
            flux.subscribe(
                    data -> {
                        try {
                            emitter.send(data);
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        try {
                            emitter.send("聊天过程中发生错误: " + error.getMessage());
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    () -> {
                        try {
                            emitter.send("聊天结束");
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    });


        }catch (Exception e){
                throw new ChatException("聊天失败: " + e.getMessage(), e);
        }*/


        Optional<NodeOutput> result = agent.invokeAndGetOutput(message,config);

        if (result.isPresent()) {
            NodeOutput output = result.get();

            if (output instanceof InterruptionMetadata interruptionMetadata) {
                handleHITL(dto, userId, agentThreadId, interruptionMetadata);
            }
            AssistantMessage response = HITLHelper.getAssistantResponse(result.get().state());
            return R.ok(response.getText());
        }
        else {
            throw new ChatException("聊天失败: 未获取到回复");
        }
    }

    @PostMapping("/stream/chatMore")
    @Operation(summary = "与烹饪助手进行功能更多的聊天（流式）", description = "向烹饪助手发送消息列表，获取回复（流式）")
    public Flux<ServerSentEvent<NodeOutput>> streamChat(@RequestBody UserChattingDTO dto) throws GraphRunnerException, IOException, InterruptedException {
        String message = dto.getMessage();


        AgentBackground cookingAssistant = AgentBackground.COOKING_ASSISTANT;

        List<ToolEntity> toolEntities = toolService.getToolEntities(dto.getToolIdList());
        // 根据用户的选项，构建对应的 Hook 列表，并将其添加到 Agent 中

        List<Hook> hookList = new LinkedList<>();
        if (dto.getHitlEnabled()) {
            hookList.add(HITLHelper.buildHITLHook(toolEntities));
        }
        if(dto.getUseRAG()){
            hookList.add(ragService.getRAGMessagesHook());
        }

        ReactAgent agent = ReactAgent.builder()
                .name(cookingAssistant.name())
                .model(chatModel)
                .outputType(String.class)
                .hooks(hookList)
                .systemPrompt(cookingAssistant.systemPrompt)
                .tools(toolFactory.selectTools(toolEntities))
                .saver(new MemorySaver())
                .build();


        Long userId = dto.getUserId();


        String agentThreadId = "user-session-" + userId;
        RunnableConfig config = RunnableConfig.builder()
                .threadId(agentThreadId)
                .build();
        Flux<NodeOutput> outputFlux = agent.stream(message, config);

        agent.stream(message, config)
                    // 3. 将数据包装成 SSE 格式 (Spring 工具类)
                    .map(data -> SseEmitter.event().data(data).build())
                    // 4. 处理错误，将其转换为 SSE 事件发送给前端，而不是中断连接
                    .onErrorResume(error -> {
                        return Flux.just(SseEmitter.event()
                                .data("Error: " + error.getMessage())
                                .build());
        });

        return null;
    }


    private void handleHITL(UserChattingDTO dto, Long userId, String agentThreadId, InterruptionMetadata interruptionMetadata) throws IOException, InterruptedException {
        // 如果发生了中断，并且中断的类型是人工介入，那么我们可以获取中断的元信息，进行相应的处理（比如通知人工审核人员进行审核）


            Long reviewerId = dto.getReviewerId();

            HITLEntity hitlEntity = new HITLEntity();

            hitlEntity.setPublisherId(userId);
            hitlEntity.setReviewerId(reviewerId);
            hitlEntity.setInterruptData(interruptionMetadata);
            hitlEntity.setThreadId(agentThreadId);
            hitlEntity.setReason("等待人工审核工具调用");


            StringBuilder remarkBuilder = new StringBuilder();

            for (InterruptionMetadata.ToolFeedback feedback : interruptionMetadata.toolFeedbacks()) {
                remarkBuilder.append("工具: ").append(feedback.getName())
                        .append(", 参数: ").append(feedback.getArguments())
                        .append(", 描述: ").append(feedback.getDescription())
                        .append("\n");

            }

            hitlEntity.setRemark(remarkBuilder.toString());

            hitlService.save(hitlEntity);

            //TODO 通知审核人 进行审核
            sseService.sendMessage(userId,reviewerId, SSEEventEnum.WAITING_REVIEW.eventName,
                    "您收到了一个新的人工审核请求，线程ID: " + agentThreadId + "，请尽快处理。");
            //TODO 通知 发布者 目前的执行状态
            sseService.sendMessage(userId, userId,SSEEventEnum.WAITING_REVIEW.eventName,
                    "您的请求正在等待人工审核，线程ID: " + agentThreadId + "，请耐心等待。");


    }


}
