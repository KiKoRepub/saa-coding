package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("tool")
@EqualsAndHashCode(callSuper = true)
public class ToolEntity extends BaseEntity{

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "工具ID")
    private Long id;

    @TableField("tool_name")
    @Schema(description = "工具名，唯一标识")
    private String toolName;

    @TableField("description")
    @Schema(description = "工具描述，介绍工具的功能和用途")
    private String description;

    @TableField("approve_description")
    @Schema(description = "HITL检查点，触发HITL的依据，比如输入内容、工具调用等")
    private String approveDescription;

    @TableField("status")
    @Schema(description = "工具状态，0-启用，1-禁用")
    private Integer status;
    @TableField("source")
    @Schema(description = "工具来源，绑定 ToolSourceEnum")
    private String source;
}
