package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
    保存用户的SSE 消息传递记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SSEUserRecord extends BaseEntity{
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "记录ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "消息发送者ID，系统消息可以为0")
    private Long userId;
    @TableField("to_id")
    @Schema(description = "消息接收方 id")
    private Long toId;
    @TableField("event_name")
    @Schema(description = "事件名称，前端根据事件名称进行不同的处理")
    private String eventName;

    @TableField("data")
    @Schema(description = "消息内容，json字符串")
    private String data;

    @TableField("has_read")
    @Schema(description = "是否已读")
    private Boolean hasRead;

    @TableField("status")
    @Schema(description = "状态,绑定 SSEUserRecordStatusEnum")
    private String status;

}
/*
CREATE TABLE `sse_user_record` (
  `id`              BIGINT       NOT NULL COMMENT '记录ID',
  `user_id`         BIGINT       DEFAULT NULL COMMENT '消息发送者ID，系统消息可以为0',
  `to_id`           BIGINT       DEFAULT NULL COMMENT '消息接收方 id',
  `event_name`      VARCHAR(64)  DEFAULT NULL COMMENT '事件名称，前端根据事件名称进行不同的处理',
  `data`            LONGTEXT     DEFAULT NULL COMMENT '消息内容，json字符串',
  `has_read`        TINYINT      DEFAULT NULL COMMENT '是否已读（0-未读，1-已读）',
  `status`          VARCHAR(32)  DEFAULT NULL COMMENT '状态,绑定 SSEUserRecordStatusEnum',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '消息发送者ID索引',
  KEY `idx_to_id` (`to_id`) USING BTREE COMMENT '消息接收方ID索引',
  KEY `idx_has_read` (`has_read`) USING BTREE COMMENT '是否已读索引',
  KEY `idx_status` (`status`) USING BTREE COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SSE消息推送记录表';
 */