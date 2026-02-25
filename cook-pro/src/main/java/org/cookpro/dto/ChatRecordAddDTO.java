package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChatRecordAddDTO {

    @Schema(name = "用户信息")
    private String userMessage;
    @Schema(name = "助手回答")
    private String botResponse;
    @Schema(name = "用户id")
    private Long userId;
    @Schema(name = "会话id")
    private String conversationId;
    @Schema(name = "调用的工具列表")
    private List<String> toolCalls;
    @Schema(name = "持久化类型")
    private String persistenceTypeCode;
    @Schema(name = "线程id")
    private String threadId;
    @Schema(name = "记录状态")
    private String recordStatus;
}
