package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChatRecordAddDTO {

    @Schema(description = "用户信息")
    private String userMessage;

    @Schema(description = "助手回答")
    private String botResponse;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "会话id")
    private String conversationId;

    @Schema(description = "调用的工具列表")
    private List<String> toolCalls;

}
