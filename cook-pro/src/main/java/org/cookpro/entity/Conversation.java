package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("conversation")
@EqualsAndHashCode(callSuper = true)
public class Conversation extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "会话ID")
    private Long id;
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    @TableField("conversation_id")
    @Schema(description = "会话ID，相同会话ID的消息属于同一次对话")
    private String conversationId;
    @TableField("title")
    @Schema(description = "会话标题")
    private String title;

    @TableField("last_message")
    @Schema(description = "最后一条消息内容")
    private String lastMessage;


}
/*
CREATE TABLE `conversation` (
  `id`              BIGINT       NOT NULL COMMENT '主键ID（会话ID）',
  `user_id`         BIGINT       DEFAULT NULL COMMENT '用户ID',
  `conversation_id` VARCHAR(255) DEFAULT NULL COMMENT '会话ID，相同会话ID的消息属于同一次对话',
  `title`           VARCHAR(512) DEFAULT NULL COMMENT '会话标题',
  `last_message`    LONGTEXT     DEFAULT NULL COMMENT '最后一条消息内容',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_user`     VARCHAR(255) DEFAULT NULL COMMENT '创建用户',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 高频查询索引：按用户ID查询会话、按会话ID查询会话
  INDEX idx_conversation_user_id (`user_id`),
  INDEX idx_conversation_conversation_id (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';
 */