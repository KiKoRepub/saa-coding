package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;

import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;

import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.FileSystemStore;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.cookpro.config.properties.UserConfigProperties;
import org.cookpro.entity.HITLToolArgInfo;
import org.cookpro.exception.ChatException;
import org.cookpro.hooks.RAGMessagesHook;
import org.cookpro.service.MemoryCacheService;
import org.cookpro.tools.alibaba.UserGetInfoTool;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.SystemPrinter;
import org.cookpro.utils.ToolUtils;
import org.cookpro.vo.ToolCallVo;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.charset.Charset;
import java.util.*;

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

    @Resource
    MemoryCacheService memoryCacheService;

    @Resource
    UserConfigProperties userConfigProperties;

    public volatile InterruptionMetadata testHitldata;

    @GetMapping("/rag")
    public void testRAG() {
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


        } catch (Exception e) {
            throw new ChatException("RAG 聊天失败: " + e.getMessage(), e);
        }

    }


    @GetMapping("humanInLoop")
    public void testHumanInLoop() throws GraphRunnerException {
        String threadId = "user-session-123";
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();
        String message = "考虑调用工具，帮我看看可乐鸡翅的配方";

        // 用户消息阶段
//        SystemPrinter.println("用户发送消息: " + message);
        messageHistory.add("用户调用阶段: 用户发送信息: " + message);

        String botResponse = "Agent 没有返回结果";

        Optional<NodeOutput> result = dashscopeHITLAgent.invokeAndGetOutput(
                message,
                config
        );
        if (result.isPresent()) {
            // TOOL CALL 阶段
            tryToolCallState(result.get().state());

            // 检查是否返回了中断
            if (result.get() instanceof InterruptionMetadata interruptionMetadata) {
                Optional<NodeOutput> finalResult = handleHITL(threadId, interruptionMetadata);

                if (finalResult.isPresent()) {

                    AssistantMessage response = HITLHelper.getAssistantResponse(finalResult.get().state());


                    SystemPrinter.println(response.getText());

                    botResponse = response.getText();
                }
            } else {
                OverAllState overAllState = result.get().state();
                botResponse = HITLHelper.getAssistantResponse(overAllState).getText();
            }

            // ASSISTANT_FINAL 阶段
            tryAssistantFinalState(result.get().state());

        }
        SystemPrinter.println("------------------------------------");
        SystemPrinter.println(messageHistory);

        SystemPrinter.println("请求执行完成");

//        return Flux.fromIterable(botResponse.getBytes())
//                .map(b -> new String(new byte[]{b}, Charset.forName("GBK")));

    }


    @GetMapping(value = "/stream/humanInLoop",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testStreamHumanInLoop() throws GraphRunnerException {

        String threadId = "stream-session-123";

        // 创建一个 sink 发送消息
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        memoryCacheService.cacheInterruptSink(threadId, sink);

        Flux<NodeOutput> outputFlux = dashscopeHITLAgent.stream("考虑调用工具，帮我看看可乐鸡翅的配方",
                RunnableConfig.builder().threadId(threadId).build());

        outputFlux.subscribe(
                out -> handleFluxChunk(threadId, sink, out),
                sink::tryEmitError,
                () -> sinkOnComplete(threadId, sink));

        return sink.asFlux();

    }


    @GetMapping(value = "/approve", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void approve() throws GraphRunnerException {


        String threadId = "stream-session-123";
        Sinks.Many<String> interruptSink = memoryCacheService.getInterruptSink(threadId);

        InterruptionMetadata approvalMetadata = getReviewResult(testHitldata);

        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                .build();

        Flux<NodeOutput> resumeFlux = dashscopeHITLAgent.stream(" ", resumeConfig);
        // 重置 testHitldata
        testHitldata = null;

        // 继续向同一个 sink 发送数据
        resumeFlux.subscribe(
                out -> handleFluxChunk(threadId, interruptSink, out),
                err -> interruptSink.tryEmitError(err),
                () -> sinkOnComplete(threadId, interruptSink)
        );


    }


    @GetMapping("/memory")
    public void testMemory() throws GraphRunnerException {

        FileSystemStore userInfoStore = new FileSystemStore(userConfigProperties.getUserDir());
        // fileSystemStore 中
        // namespace = 文件夹  key = 文件名 value = 文件内容
        // namespace = List  => 多级文件夹路径



// 创建获取用户信息的工具
//        BiFunction<GetMemoryRequest, ToolContext, MemoryResponse> getUserInfoFunction =
//                (request, context) -> {
//                    RunnableConfig runnableConfig = (RunnableConfig) context.getContext().get("_AGENT_CONFIG_");
//                    Store store = runnableConfig.store();
//                    Optional<StoreItem> itemOpt = store.getItem(request.namespace(), request.key());
//                    if (itemOpt.isPresent()) {
//                        Map<String, Object> value = itemOpt.get().getValue();
//                        return new MemoryResponse("找到用户信息", value);
//                    }
//                    return new MemoryResponse("未找到用户", Map.of());
//                };
//
//        ToolCallback getUserInfoTool = FunctionToolCallback.builder("getUserInfo", getUserInfoFunction)
//                .description("查询用户信息")
//                .inputType(GetMemoryRequest.class)
//                .build();

// 创建Agent
        ReactAgent agent = ReactAgent.builder()
                .name("memory_agent")
                .model(chatModel)
                .tools(ToolUtils.buildToolCallback(new UserGetInfoTool()))
                .saver(new MemorySaver())
                .build();

// 向存储中写入示例数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("userName", "张三");
        userData.put("language", "中文");

        StoreItem userItem = StoreItem.of(List.of("info"), "user_123", userData);
        userInfoStore.putItem(userItem);

// 运行Agent
        RunnableConfig config = RunnableConfig.builder()
                .threadId("session_001")
                .addMetadata("user_id", "user_123")
                .addMetadata("namespace", List.of("info"))
                .store(userInfoStore)
                .build();
        String msgPrefix = "所有 在记忆操作中需要的数据都在当前运行配置 (RunnableConfig) 对象中，例如 namespace,user_id 帮我完成以下的任务:";
        Optional<OverAllState> invoke = agent.invoke(msgPrefix + "查询用户信息，namespace=['info'], key='user_123'", config);

        invoke.ifPresent(state -> SystemPrinter.println("Agent 执行完成，最终状态: " + state));
    }


    private Flux<String>  convertToStringFlux(Flux<NodeOutput> outputFlux){
        return outputFlux.flatMap(out -> {
            if (out instanceof InterruptionMetadata interruptionMetadata) {
                // 处理人工介入中断
                testHitldata = interruptionMetadata;
                return Flux.just("[系统] 发生了工具调用中断，请等待人工审核结果...");
            } else if (out instanceof StreamingOutput<?> streamingOutput) {
                // 处理流式输出
                String chunk = streamingOutput.chunk();
                if (chunk != null && !chunk.isEmpty()) {
//                    SystemPrinter.println("流式chunk: " + chunk);
                    return Flux.just(chunk);
                }
            }
            return Flux.empty();
        });
    }

    private Optional<NodeOutput> handleHITL(String threadId, InterruptionMetadata interruptionMetadata) throws GraphRunnerException {

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
        SystemPrinter.println(" === hitl 调用：使用批准决策恢复 ===");
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                .build();

        Optional<NodeOutput> finalResult = dashscopeHITLAgent.invokeAndGetOutput(" ", resumeConfig);


        // TOOL_RESULT 阶段 可能再次触发中断
        tryToolResultState(finalResult.get().state());
        // 可能进入 TOOL_CALL 阶段
        tryToolCallState(finalResult.get().state());

        if (finalResult.isPresent() && finalResult.get() instanceof InterruptionMetadata) {
            SystemPrinter.println("再次触发人工介入中断........");
            return handleHITL(threadId, (InterruptionMetadata) finalResult.get());
        }


        return finalResult;
    }

    private void tryToolCallState(OverAllState state) {

        try {
            AssistantMessage message = HITLHelper.getAssistantResponse(state);

            if (HITLHelper.isToolCall(message)) {
                // 进入 TOOL_CALL 阶段
//                SystemPrinter.println("进入 TOOL_CALL 阶段，等待工具调用结果...");
                ToolCallVo toolCallVo = ToolUtils.serializeToolCall(message.getToolCalls().get(0));
                messageHistory.add("工具调用阶段: Agent 决定调用工具 :" + toolCallVo.getToolName() + "，参数: " + toolCallVo.getArguments());
            }
        } catch (Exception e) {
            SystemPrinter.println("没有找到 AssistantMessage，未进入 TOOL_CALL 阶段");
        }
    }

    private static void tryAuditState() {
        messageHistory.add("审核阶段 : 人工审核工具调用请求");
    }

    private void tryToolResultState(OverAllState state) {
        ToolResponseMessage toolResponse = HITLHelper.getToolResponse(state);
        toolResponse.getResponses().forEach(response -> {
            SystemPrinter.println("工具返回结果: " + response.responseData());
        });
        messageHistory.add("工具结果阶段: 工具返回结果有 " + toolResponse.getResponses().size() + " 条");

    }

    private void tryAssistantFinalState(OverAllState state) {
        AssistantMessage message = HITLHelper.getAssistantResponse(state);
        if (message != null) {
            messageHistory.add("助手最终回复阶段: " + message.getText());
        } else {
            SystemPrinter.println("没有找到 AssistantMessage，未进入 ASSISTANT_FINAL 阶段");
        }
    }

    private static InterruptionMetadata getReviewResult(InterruptionMetadata interruptionMetadata) {
        // 6. 模拟人工决策
        InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();

        toolFeedbacks.forEach(toolFeedback -> {

            if (toolFeedback.getName().equals("google_web_search")) {
                //
                List<HITLToolArgInfo> infoList = new ArrayList<>();

                infoList.add(new HITLToolArgInfo("arg0", "可乐鸡翅的做法(made by human)"));
                infoList.add(new HITLToolArgInfo("arg1", "3"));
                feedbackBuilder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                                .build()
                );
            } else {
                InterruptionMetadata.ToolFeedback approvedFeedback =
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build();
                feedbackBuilder.addToolFeedback(approvedFeedback);
            }

        });
        // 进入 审核阶段
        tryAuditState();
        return feedbackBuilder.build();
    }


    private void handleFluxChunk(String threadId, Sinks.Many<String> sink, NodeOutput out) {
        if (out instanceof InterruptionMetadata interruptionMetadata) {
            for (InterruptionMetadata.ToolFeedback toolFeedback : interruptionMetadata.toolFeedbacks()) {
                String toolName = toolFeedback.getName();

                SystemPrinter.println("工具调用中断: " + toolName);
            }
            if (memoryCacheService.hasInterruptSink(threadId)) {
                sink.tryEmitNext("[系统] 发生了工具调用中断，请等待人工审核结果...");
                testHitldata = interruptionMetadata;
            } else {
                sink.tryEmitNext("[系统] 系统出现异常，无法处理人工审核，请稍后再试...");
                sink.tryEmitComplete();
            }
        }
        if (out instanceof StreamingOutput<?> streamingOutput) {

            String chunk = streamingOutput.chunk();

            if (chunk != null && !chunk.isEmpty()) {

                SystemPrinter.println("流式chunk: " + chunk);

                sink.tryEmitNext(chunk);
            }
        }
    }

    private void sinkOnComplete(String threadId, Sinks.Many<String> sink) {
        if (testHitldata == null) {
            SystemPrinter.println("任务真正完成，关闭流");
            sink.tryEmitComplete();
            memoryCacheService.removeInterruptSink(threadId);
        } else {
            SystemPrinter.println("检测到中断，流保持开启，等待审核...");
        }
    }

    private static LinkedList<Object> messageHistory = new LinkedList<>();


}
