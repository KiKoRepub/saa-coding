package org.cookpro.entity;

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

    @TableId
    @Schema(description = "ID")
    private Integer id;

    @TableField("user_id")
    @Schema(description = "User ID")
    private Long userId;

    @TableField("conversation_id")
    @Schema(description = "会话id")
    private String conversationId;

    @TableField("user_message")
    @Schema(description = "User Message")
    private String userMessage;

    @TableField("bot_response")
    @Schema(description = "Bot Response")
    private String botResponse;
    @TableField("thread_id")
    @Schema(description = "Thread ID, 用于关联同一对话中的多个消息")
    private String threadId;

    @TableField(value = "tool_calls",typeHandler = ListStringTypeHandler.class)
    @Schema(description = "工具调用记录")
    private List<String> toolCalls;
    @TableField("record_status")
    @Schema(description = "记录状态,绑定 ChatRecordStatusEnum")
    private String recordStatus;

}
