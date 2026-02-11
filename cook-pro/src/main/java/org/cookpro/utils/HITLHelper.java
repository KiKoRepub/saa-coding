package org.cookpro.utils;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.exception.AgentException;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import org.cookpro.entity.HITLToolArgInfo;
import org.cookpro.entity.ToolEntity;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;
import java.util.Optional;

public class HITLHelper {



    public static HumanInTheLoopHook buildHITLHook(List<ToolEntity> toolList){

        HumanInTheLoopHook.Builder hookBuilder = HumanInTheLoopHook.builder();

        for (ToolEntity toolEntity : toolList) {
            hookBuilder.approvalOn(toolEntity.getToolName(), ToolConfig.builder()
                    .description(toolEntity.getDescription())
                    .build());
        }

        return hookBuilder.build();
    }
    /**
     * 批准所有工具调用
     */
    public static InterruptionMetadata approveAll(InterruptionMetadata interruptionMetadata) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            builder.addToolFeedback(
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                            .build()
            );
        });

        return builder.build();
    }

    /**
     * 拒绝所有工具调用
     */
    public static InterruptionMetadata rejectAll(
            InterruptionMetadata interruptionMetadata,
            String reason) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            builder.addToolFeedback(
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                            .description(reason)
                            .build()
            );
        });

        return builder.build();
    }

    /**
     * 编辑特定工具的参数
     */
    public static InterruptionMetadata editTool(
            InterruptionMetadata interruptionMetadata,
            String toolName,
            String newArguments) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            if (toolFeedback.getName().equals(toolName)) {
                builder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .arguments(newArguments)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.EDITED)
                                .build()
                );
            } else {
                builder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build()
                );
            }
        });

        return builder.build();
    }


    public static AssistantMessage getAssistantResponse(OverAllState state){
        Optional<OverAllState> optionalState = Optional.of(state);
        return optionalState.flatMap(s -> s.value("messages"))
                .stream()
                .flatMap(messageList -> ((List<?>) messageList).stream()
                        .filter(msg -> msg instanceof AssistantMessage)
                        .map(msg -> (AssistantMessage) msg))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AgentException("No AssistantMessage found in 'messages' state"));

    }



    public static void main(String[] args) {

        InterruptionMetadata interruptionMetadata = null; // 假设这是从中断中获得的元数据

        // 使用示例
        InterruptionMetadata approvalMetadata = HITLHelper.approveAll(interruptionMetadata);
        InterruptionMetadata rejectMetadata = HITLHelper.rejectAll(interruptionMetadata, "操作不安全");
        InterruptionMetadata editMetadata = HITLHelper.editTool(
                interruptionMetadata,
                "execute_sql",
                "{\"query\": \"SELECT * FROM records LIMIT 10\"}"
        );


        System.out.println(buildEditedArguments(List.of(
                new HITLToolArgInfo("query", "SELECT * FROM records LIMIT 10"),
                new HITLToolArgInfo("timeout", "30s")
        )));
    }

    public static String buildEditedArguments(List<HITLToolArgInfo> infoList) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (HITLToolArgInfo argInfo : infoList) {
            String argName = argInfo.getArgName();
            String argValue = argInfo.getArgValue();

            String formattedArg = "\"" + argName + "\": \"" + argValue + "\"";

            sb.append(formattedArg).append(", ");
        }

        sb.deleteCharAt(sb.length() - 2); // 删除最后一个逗号和空格

        sb.append(" }");

        return sb.toString();
    }
}

