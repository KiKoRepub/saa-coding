package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ToolPageDTO extends BasePageDTO {

    @Schema(description = "工具名，模糊查询")
    private String toolName;


}
