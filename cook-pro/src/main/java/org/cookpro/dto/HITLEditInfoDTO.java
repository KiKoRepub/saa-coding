package org.cookpro.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.cookpro.entity.HITLToolArgInfo;

import java.util.List;

@Data
public class HITLEditInfoDTO {

    @Schema(description = "人工介入任务的id")
    private Long id;

    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "参数信息列表")
    private List<HITLToolArgInfo> argInfoList;

    @Schema(description = "工具名称")
    private String toolName;
}
