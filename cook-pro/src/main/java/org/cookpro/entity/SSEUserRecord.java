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
