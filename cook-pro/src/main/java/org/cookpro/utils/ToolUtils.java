package org.cookpro.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.LinkedList;
import java.util.List;
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

}
