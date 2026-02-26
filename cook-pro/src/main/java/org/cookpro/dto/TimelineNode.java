package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
public class TimelineNode {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "对话线程id")
    private String threadId;
    @Schema(description = "记录类型，USER / ASSISTANT / TOOL / AUDIT")
    private String recordType;

    @Schema(description = "内容，用户消息、助手回复、工具调用信息或审核信息")
    private String content;

    @Schema(description = "HITL审核状态，审核通过、待审核、审核拒绝等")
    private String hitlStatus;

    @Schema(description = "记录创建时间")
    private LocalDateTime time;

}