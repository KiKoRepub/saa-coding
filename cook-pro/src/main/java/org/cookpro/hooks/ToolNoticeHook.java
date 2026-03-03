package org.cookpro.hooks;


import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import org.cookpro.service.MemoryCacheService;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.ToolUtils;
import org.cookpro.vo.ToolCallVo;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@HookPositions(HookPosition.AFTER_MODEL)
public class ToolNoticeHook extends ModelHook {
    @Autowired
    MemoryCacheService memoryCacheService;
    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        AssistantMessage assistantResponse = HITLHelper.getAssistantResponse(state);

        if (assistantResponse.hasToolCalls()){
            // 包含工具调用
            String threadId = config.threadId().get();
            if (memoryCacheService.hasInterruptSink(threadId)){

                Sinks.Many<String> sink = memoryCacheService.getInterruptSink(threadId);
                // 发送 通知
                List<AssistantMessage.ToolCall> toolCalls = assistantResponse.getToolCalls();

                memoryCacheService.cacheToolCallHistory(threadId, toolCalls);

                List<ToolCallVo> toolCallVo = ToolUtils.serializeToolCalls(toolCalls);

                for (ToolCallVo callVo : toolCallVo) {
                    sink.tryEmitNext("调用工具:" + callVo.getToolName() + "......\n");
                }

            }
        }

        return super.afterModel(state,config);
    }
}
