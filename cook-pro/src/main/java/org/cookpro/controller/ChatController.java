package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
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
import org.cookpro.service.SSEService;
import org.cookpro.service.ToolService;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.SystemPrinter;
import org.cookpro.utils.ToolFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

        ReactAgent agent = ReactAgent.builder()
                .name(cookingAssistant.name())
                .model(chatModel)
                .outputType(String.class)
                .hooks(HITLHelper.buildHITLHook(toolEntities))
                .systemPrompt(cookingAssistant.systemPrompt)
                .tools(toolFactory.selectTools(toolEntities))
                .saver(new MemorySaver())
                .build();

        Long userId = dto.getUserId();


        String agentThreadId = "user-session-" + userId;
        RunnableConfig config = RunnableConfig.builder()
                .threadId(agentThreadId)
                .build();

        Optional<NodeOutput> result = agent.invokeAndGetOutput(message,config);
        if (result.isPresent()) {
            NodeOutput output = result.get();
            // 如果发生了中断，并且中断的类型是人工介入，那么我们可以获取中断的元信息，进行相应的处理（比如通知人工审核人员进行审核）
            if (output instanceof InterruptionMetadata interruptionMetadata) {

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
                sseService.sendMessage(userId,userId,SSEEventEnum.WAITING_REVIEW.eventName,
                        "您的请求正在等待人工审核，线程ID: " + agentThreadId + "，请耐心等待。");

            }

            AssistantMessage response = HITLHelper.getAssistantResponse(result.get().state());
            return R.ok(response.getText());
        }
        else {
            throw new ChatException("聊天失败: 未获取到回复");
        }
    }


}
