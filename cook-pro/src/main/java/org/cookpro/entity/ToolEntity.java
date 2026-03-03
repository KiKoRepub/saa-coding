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
  `tool_name`       VARCHAR(64)  NOT NULL COMMENT '工具名，唯一标识',
  `description`     VARCHAR(512) DEFAULT NULL COMMENT '工具描述，介绍工具的功能和用途',
  `audit_remark`    VARCHAR(512) DEFAULT NULL COMMENT 'HITL检查点，触发HITL的依据，比如输入内容、工具调用等',
  `auditor_id`     BIGINT       DEFAULT NULL COMMENT '审核人ID，关联用户表',
  `status`          INT          DEFAULT NULL COMMENT '工具状态，0-启用，1-禁用',
  `source`          VARCHAR(32)  DEFAULT NULL COMMENT '工具来源，绑定 ToolSourceEnum',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tool_name` (`tool_name`) USING BTREE COMMENT '工具名唯一索引',
  KEY `idx_auditor_id` (`auditor_id`) USING BTREE COMMENT '审核人ID索引',
  KEY `idx_status` (`status`) USING BTREE COMMENT '工具状态索引',
  KEY `idx_source` (`source`) USING BTREE COMMENT '工具来源索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具信息表';
 */