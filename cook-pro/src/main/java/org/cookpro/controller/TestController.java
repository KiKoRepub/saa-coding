package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.SpringAIJacksonStateSerializer;
import com.alibaba.druid.wall.WallConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.cookpro.config.properties.ToolEnvProperties;
import org.cookpro.exception.ChatException;
import org.cookpro.hooks.RAGMessagesHook;
import org.cookpro.tools.WebSearchTool;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.SystemPrinter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;

@RestController
@RequestMapping("/test")
@Tag(name = "TestController", description = "测试接口")
public class TestController {
    @Resource
    DashScopeChatModel chatModel;

    @Resource
    VectorStore vectorStore;

    @Resource
    ReactAgent dashscopeHITLAgent;


    @GetMapping("/rag")
    public void testRAG(){
        // 在 agent 执行前 查询向量数据库，获取相关文档，并将其添加到消息中
        // 创建带有 RAG Hook 的 Agent
        ReactAgent ragAgent = ReactAgent.builder()
                .name("rag_agent")
                .model(chatModel)
                .hooks(new RAGMessagesHook(vectorStore))
                .build();

        // 调用 Agent
        try {
            AssistantMessage response = ragAgent.call("番茄炒蛋应该怎么制作？");

            String result = new String(("答案: " + response.getText()).getBytes(), Charset.forName("GBK"));
            SystemPrinter.println(result);


        }catch (Exception e){
            throw new ChatException("RAG 聊天失败: " + e.getMessage(), e);
        }

    }


    @GetMapping("humanInLoop")
    public void testHumanInLoop() throws GraphRunnerException {




// 人工介入利用检查点机制。
// 你必须提供线程ID以将执行与会话线程关联，
// 以便可以暂停和恢复对话（人工审查所需）。
        String threadId = "user-session-123"; // [!code highlight]
        RunnableConfig config = RunnableConfig.builder() // [!code highlight]
                .threadId(threadId) // [!code highlight]
                .build(); // [!code highlight]
        String message = "考虑调用工具，帮我看看可乐鸡翅的配方";
// 运行图直到触发中断
        Optional<NodeOutput> result = dashscopeHITLAgent.invokeAndGetOutput( // [!code highlight]
                message,
                config
        );

// 检查是否返回了中断
        if (result.isPresent() && result.get() instanceof InterruptionMetadata interruptionMetadata) {

            SystemPrinter.println("触发了人工介入中断，等待审查...");
            // 中断包含需要审查的工具反馈
            List<InterruptionMetadata.ToolFeedback> toolFeedbacks =
                    interruptionMetadata.toolFeedbacks();

            for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                SystemPrinter.println("工具: " + feedback.getName());
                SystemPrinter.println("参数: " + feedback.getArguments());
                SystemPrinter.println("描述: " + feedback.getDescription());
            }



            // 设置 审批结果并恢复执行

            InterruptionMetadata approvalMetadata = getReviewResult(interruptionMetadata);

            // 7. 第二次调用 - 使用人工反馈恢复执行
            SystemPrinter.println(" === 第二次调用：使用批准决策恢复 ===");
            RunnableConfig resumeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                    .build();

            Optional<NodeOutput> finalResult = dashscopeHITLAgent.invokeAndGetOutput(" ", resumeConfig);

            if (finalResult.isPresent()) {
                SystemPrinter.println("执行完成");
                SystemPrinter.println("最终结果: " + finalResult.get());

                AssistantMessage response = HITLHelper.getAssistantResponse(finalResult.get().state());

                System.out.println(response.getText());
            }
            // 示例输出:
            // 工具: execute_sql
            // 参数: {"query": "DELETE FROM records WHERE created_at < NOW() - INTERVAL '30 days';"}
            // 描述: SQL执行操作需要审批
        }else {
            OverAllState overAllState = result.get().state();
            SystemPrinter.println("没有触发人工介入中断，结果: " + HITLHelper.getAssistantResponse(overAllState).getText());
        }
    }

    private static InterruptionMetadata getReviewResult(InterruptionMetadata interruptionMetadata) {
        // 6. 模拟人工决策（这里选择批准）
        InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();

        toolFeedbacks.forEach(toolFeedback -> {
            InterruptionMetadata.ToolFeedback approvedFeedback =
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                            .build();
            feedbackBuilder.addToolFeedback(approvedFeedback);
        });

        InterruptionMetadata approvalMetadata = feedbackBuilder.build();
        return approvalMetadata;
    }
}
