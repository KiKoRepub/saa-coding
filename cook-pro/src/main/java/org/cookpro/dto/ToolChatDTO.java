package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ToolChatDTO {

    @Schema(description = "工具名，唯一标识")
    private String toolName;
    @Schema(description = "工具来源，绑定 ToolSourceEnum")
    private String source;
    @Schema(description = "工具描述，介绍工具的功能和用途")
    private String auditRemark;
    @Schema(description = "工具的审核人id")
    private Long auditorId;


}
