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

    @TableField("audit_remark")
    @Schema(description = "HITL检查点，触发HITL的依据，比如输入内容、工具调用等")
    private String auditRemark;
    @TableField("auditor_id")
    @Schema(description = "审核人ID，关联用户表")
    private Long auditorId;
    @TableField("status")
    @Schema(description = "工具状态，0-启用，1-禁用")
    private Integer status;
    @TableField("source")
    @Schema(description = "工具来源，绑定 ToolSourceEnum")
    private String source;
}
/*
CREATE TABLE `tool` (
  `id`              BIGINT       NOT NULL COMMENT '工具ID',
  `tool_name`       VARCHAR(255) NOT NULL COMMENT '工具名，唯一标识',
  `description`     TEXT         DEFAULT NULL COMMENT '工具描述，介绍工具的功能和用途',
  `audit_remark`    TEXT         DEFAULT NULL COMMENT 'HITL检查点，触发HITL的依据，比如输入内容、工具调用等',
  `auditor_id`      BIGINT       DEFAULT NULL COMMENT '审核人ID，关联用户表',
  `status`          TINYINT      NOT NULL DEFAULT '0' COMMENT '工具状态，0-启用，1-禁用',
  `source`          VARCHAR(255) DEFAULT NULL COMMENT '工具来源，绑定 ToolSourceEnum',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_user`     VARCHAR(255) DEFAULT NULL COMMENT '创建用户',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 唯一索引保证工具名唯一
  UNIQUE INDEX uk_tool_tool_name (`tool_name`),
  -- 普通索引提升关联/查询效率
  INDEX idx_tool_auditor_id (`auditor_id`),
  INDEX idx_tool_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具表';
 */