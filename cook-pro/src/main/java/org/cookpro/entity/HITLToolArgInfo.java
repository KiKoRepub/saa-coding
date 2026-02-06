package org.cookpro.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HITLToolArgInfo {
    @Schema(description = "参数名称")
    private String argName;
    @Schema(description = "参数值")
    private String argValue;

}
