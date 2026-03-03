package org.cookpro.interceptors;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToolResultCacheInterceptor extends ToolInterceptor {

    private final Map<String, ToolCallResponse> cache = new ConcurrentHashMap<>();

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        String cacheKey = generateCacheKey(request);

// 检查缓存
        ToolCallResponse cached = cache.get(cacheKey);
        if (cached != null && !isExpired(cached)) {
            System.out.println("缓存命中: " + request.getToolName());
            return cached;
        }

// 执行工具
        ToolCallResponse response = handler.call(request);

// 缓存结果
        cache.put(cacheKey, response);

        return response;
    }

    @Override
    public String getName() {
        return "ToolCacheInterceptor";
    }

    private String generateCacheKey(ToolCallRequest request) {
        return request.getToolName() + ":" +
                request.getArguments();
    }

    private boolean isExpired(ToolCallResponse response) {
//TODO 实现 TTL 检查逻辑
        return false;
    }
}