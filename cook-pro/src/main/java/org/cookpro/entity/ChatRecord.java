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





CREATE INDEX idx_thread_id ON chat_record(thread_id);

CREATE INDEX idx_conversation_id ON chat_record(conversation_id);

CREATE INDEX idx_user_id ON chat_record(user_id);
 */