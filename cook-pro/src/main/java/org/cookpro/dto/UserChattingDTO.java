package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class UserChattingDTO {

    @Schema(description = "用户输入的消息")
    private String message;
    @Schema(description = "使用的工具列表")
    private List<String> toolNameList;
    @Schema(description = "审核人id(触发HITL)")
    private Long reviewerId;

}
