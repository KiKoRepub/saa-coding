package org.cookpro.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.w3c.dom.stylesheets.LinkStyle;

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

    public static String extractToolName(String toolCallJson){
        try {
            AssistantMessage.ToolCall call = objectMapper.readValue(toolCallJson, AssistantMessage.ToolCall.class);
            return call.name();
        }catch (Exception e){
            throw new RuntimeException("工具调用反序列化失败", e);
        }

    }
    public static Map<String,Object> extractToolArguments(String toolCallJson){
        try {
            AssistantMessage.ToolCall call = objectMapper.readValue(toolCallJson, AssistantMessage.ToolCall.class);

            return objectMapper.convertValue(call.arguments(), Map.class);
        }catch (Exception e){
            throw new RuntimeException("工具调用反序列化失败", e);
        }

    }

}
