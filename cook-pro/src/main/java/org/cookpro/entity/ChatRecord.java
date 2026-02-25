package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_record")
public class ChatRecord {

    @TableId
    @Schema(description = "ID")
    private Integer id;

    @TableField("user_id")
    @Schema(description = "User ID")
    private String userId;

    @TableField("conversation_id")
    @Schema(description = "Conversation ID")
    private String conversationId;

    @TableField("user_message")
    @Schema(description = "User Message")
    private String userMessage;

    @TableField("bot_response")
    @Schema(description = "Bot Response")
    private String botResponse;

    @TableField("created_at")
    @Schema(description = "Creation Timestamp")
    private LocalDateTime createdAt;

    @TableField("persistence_type_code")
    @Schema(description = "Persistence Type: auto-自动持久化, manual-手动持久化")
    private String persistenceTypeCode;

    @TableField("persistence_time")
    @Schema(description = "Persistence Time")
    private LocalDateTime persistenceTime;

}
