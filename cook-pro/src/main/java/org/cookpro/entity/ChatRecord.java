package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.cookpro.handler.ListStringTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("chat_record")
public class ChatRecord extends BaseEntity {

        @TableId(type = IdType.ASSIGN_ID)
        @Schema(description = "聊天记录ID")
        private Long id;

        @TableField("user_id")
        @Schema(description = "用户ID")
        private Long userId;

        @TableField("conversation_id")
        @Schema(description = "会话ID，相同会话ID的消息属于同一次对话")
        private String conversationId;
        /**
         * 用户消息
         */
        @TableField("user_message")
        private String userMessage;

        /**
         * assistant 回复
         */
        @TableField("bot_response")
        private String botResponse;

        /**
         * tool call json
         */
        @TableField(value = "tool_call")
        private String toolCall;

}
/*

CREATE TABLE `chat_record` (
  `id`              BIGINT       NOT NULL COMMENT '聊天记录ID',
  `user_id`         BIGINT       DEFAULT NULL COMMENT '用户ID',
  `conversation_id` VARCHAR(64)  DEFAULT NULL COMMENT '会话ID，相同会话ID的消息属于同一次对话',
  `user_message`    TEXT         DEFAULT NULL COMMENT '用户消息',
  `bot_response`    TEXT         DEFAULT NULL COMMENT 'assistant 回复',
  `tool_call`       TEXT         DEFAULT NULL COMMENT 'tool call json',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
  KEY `idx_conversation_id` (`conversation_id`) USING BTREE COMMENT '会话ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

 */