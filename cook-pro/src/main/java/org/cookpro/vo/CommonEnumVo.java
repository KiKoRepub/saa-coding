package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommonEnumVo {
    @Schema(description = "展示值")
    private String toShow;
    @Schema(description = "传递值")
    private String toTransfer;
}
