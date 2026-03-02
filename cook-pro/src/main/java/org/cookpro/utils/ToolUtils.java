package org.cookpro.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cookpro.vo.ToolCallVo;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToolUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> convertToolCall(List<AssistantMessage.ToolCall> toolCalls){
        return toolCalls.stream()
                .map(toolCall -> {
                    try {
                        return objectMapper.writeValueAsString(toolCall);
                    } catch (Exception e) {
                        throw new RuntimeException("工具调用序列化失败", e);
                    }
                }).collect(Collectors.toList());
    }
    public static List<ToolCallVo> serializeToolCalls(List<AssistantMessage.ToolCall> toolCallList) {

        // 2. 如果集合为空，直接返回空列表，避免 NullPointerException
        if (toolCallList == null) {
            return Collections.emptyList();
        }

        // 3. 使用 Stream 流式处理每个 ToolCall 对象并转换成 Vo
        return toolCallList.stream()
                .map(ToolUtils::serializeToolCall) // 调用你现有的单个转换方法
                .collect(Collectors.toList());
    }
    public static List<ToolCallVo> serializeToolCalls(String toolCallsJson) {
        try {
            // 1. 将 JSON 反序列化为 List<AssistantMessage.ToolCall>
            // 这里使用 TypeReference 来处理泛型
            List<AssistantMessage.ToolCall> calls = objectMapper.readValue(
                    toolCallsJson,
                    new TypeReference<List<AssistantMessage.ToolCall>>() {}
            );

            return serializeToolCalls(calls);

        } catch (Exception e) {
            throw new RuntimeException("工具调用列表反序列化失败", e);
        }
    }
     public static ToolCallVo serializeToolCall(AssistantMessage.ToolCall toolCall){
        try {
            ToolCallVo toolCallVo = new ToolCallVo();
            toolCallVo.setToolName(toolCall.name());
            String argsJson = String.valueOf(toolCall.arguments());
            Map<String, Object> argumentsMap = objectMapper.readValue(
                    argsJson,
                    new TypeReference<Map<String, Object>>() {}
            );
            toolCallVo.setArguments(argumentsMap);
            return toolCallVo;

        }catch (Exception e){
            throw new RuntimeException("工具调用反序列化失败", e);
        }

     }
}
