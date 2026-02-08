package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
@Data
public class ToolPageListVo {

    @Schema(description = "工具名")
    private String toolName;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "是不是项目定义的工具")
    private int isProjectTool;

    @Schema(description = "创建用户")
    private String createUser;

}
