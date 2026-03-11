package org.cookpro.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.cookpro.entity.HITLToolArgInfo;

import java.util.List;

@Data
public class ToolAutoPersistDTO {

    @Schema
    private String source;


    @Schema(description = "工具名")
    private String toolName;

    @Schema(description = "工具描述，介绍工具的功能和用途")
    private String description;


}
