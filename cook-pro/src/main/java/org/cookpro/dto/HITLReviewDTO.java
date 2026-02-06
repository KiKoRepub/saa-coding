package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HITLReviewDTO {
    @Schema(description = "人工介入任务的id")
    private Long id;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "是否通过审核")
    private boolean approved;




}
