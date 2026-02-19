package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class UserChattingDTO {

    @Schema(description = "用户输入的消息")
    private String message;
    @Schema(description = "使用的工具列表")
    private List<Long> toolIdList;

    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "审核人id(触发HITL)")
    private Long reviewerId;
    @Schema(description = "是否启用HITL")
    private Boolean hitlEnabled;
    @Schema(description = "是否使用RAG")
    private Boolean useRAG;

}
