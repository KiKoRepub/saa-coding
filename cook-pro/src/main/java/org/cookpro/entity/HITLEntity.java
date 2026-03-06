package org.cookpro.entity;

import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cookpro.handler.InterruptDataSerialHandler;

@Data
@TableName("hitl")
@EqualsAndHashCode(callSuper = true)
public class HITLEntity extends BaseEntity{
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("review_id")
    @Schema(description = "审核人ID")
    private Long reviewerId;

    @TableField("publisher_id")
    @Schema(description = "发布人ID")
    private Long publisherId;

    @TableField("interrupt_id")
    @Schema(description = "中断ID(线程id)")
    private String threadId;

    @TableField(value = "interrupt_data",typeHandler = InterruptDataSerialHandler.class)
    @Schema(description = "中断数据")
    private InterruptionMetadata interruptData;

    @TableField("reason")
    @Schema(description = "审核原因")
    private String reason;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @TableField("status")
    @Schema(description = "审核状态")
    private String status;

}
/*
CREATE TABLE `interrupt_review` (
  `id`              BIGINT       NOT NULL COMMENT '主键ID',
  `review_id`       BIGINT       DEFAULT NULL COMMENT '审核人ID',
  `publisher_id`    BIGINT       DEFAULT NULL COMMENT '发布人ID',
  `interrupt_id`    VARCHAR(255) DEFAULT NULL COMMENT '中断ID(线程id)',
  `interrupt_data`  LONGTEXT     DEFAULT NULL COMMENT '中断数据（序列化存储）',
  `reason`          TEXT         DEFAULT NULL COMMENT '审核原因',
  `remark`          TEXT         DEFAULT NULL COMMENT '备注',
  `status`          VARCHAR(255) DEFAULT NULL COMMENT '审核状态',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_user`     VARCHAR(255) DEFAULT NULL COMMENT '创建用户',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 高频查询索引，提升审核人/发布人/状态筛选效率
  INDEX idx_interrupt_review_review_id (`review_id`),
  INDEX idx_interrupt_review_publisher_id (`publisher_id`),
  INDEX idx_interrupt_review_status (`status`),
  INDEX idx_interrupt_review_interrupt_id (`interrupt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HITL中断审核表';
 */
