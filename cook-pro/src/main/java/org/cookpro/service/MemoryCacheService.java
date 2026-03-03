package org.cookpro.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
  内存缓存服务，用于一些特殊数据的缓存
    1. Sinks.Many<String> 用于线程中断的信号传递
    2. 工具调用历史
 */
@Service
public class MemoryCacheService {


    // 所有缓存项
    private final Map<String, Sinks.Many<String>> threadInterruptSinks = new ConcurrentHashMap<>();
    private final Map<String, List<AssistantMessage.ToolCall>> toolCallHistory = new ConcurrentHashMap<>();


    public void cacheInterruptSink(String threadId, Sinks.Many<String> sink) {
        threadInterruptSinks.put(threadId, sink);
    }

    public boolean hasInterruptSink(String threadId) {
        return threadInterruptSinks.containsKey(threadId);
    }
    public Sinks.Many<String> getInterruptSink(String threadId) {
        return threadInterruptSinks.get(threadId);
    }
    public void removeInterruptSink(String threadId) {
        threadInterruptSinks.remove(threadId);
    }

    public  void  cacheToolCallHistory(String threadId, AssistantMessage.ToolCall toolCalls) {
        // 每次调用都追加到列表中
        List<AssistantMessage.ToolCall> callList = toolCallHistory.get(threadId);
        if (callList == null) {
            callList = new java.util.ArrayList<>();
        }
        callList.add(toolCalls);
        toolCallHistory.put(threadId, callList);
    }
    public  void  cacheToolCallHistory(String threadId, List<AssistantMessage.ToolCall> toolCalls) {
        // 线程安全(代码逻辑决定)
        List<AssistantMessage.ToolCall> callList = toolCallHistory.get(threadId);
        if (callList == null) {
            callList = new java.util.ArrayList<>();
        }
        callList.addAll(toolCalls);
        toolCallHistory.put(threadId, callList);
    }
    public List<AssistantMessage.ToolCall> getToolCallHistory(String threadId) {
        return toolCallHistory.getOrDefault(threadId, List.of());
    }

    public void clearToolCallHistory(String threadId) {
        toolCallHistory.remove(threadId);
    }
}
