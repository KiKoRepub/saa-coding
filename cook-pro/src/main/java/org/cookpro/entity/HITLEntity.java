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
@TableName("interrupt_review")
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
  `interrupt_id`    VARCHAR(64)  DEFAULT NULL COMMENT '中断ID(线程id)',
  `interrupt_data`  LONGTEXT     DEFAULT NULL COMMENT '中断数据',
  `reason`          VARCHAR(512) DEFAULT NULL COMMENT '审核原因',
  `remark`          VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `status`          VARCHAR(32)  DEFAULT NULL COMMENT '审核状态',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`) USING BTREE COMMENT '审核人ID索引',
  KEY `idx_publisher_id` (`publisher_id`) USING BTREE COMMENT '发布人ID索引',
  KEY `idx_interrupt_id` (`interrupt_id`) USING BTREE COMMENT '中断ID索引',
  KEY `idx_status` (`status`) USING BTREE COMMENT '审核状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中断审核记录表';
 */
