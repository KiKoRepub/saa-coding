package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.security.DenyAll;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("tool_execution")
@EqualsAndHashCode(callSuper = true)
public class ToolExecution extends BaseEntity{

//    | 字段              | 说明                       |
//| --------------- | ------------------------ |
//| id              | 执行ID                     |
//| conversation_id | 会话                       |
//| message_id      | 触发的消息                    |
//| tool_id         | 调用的工具                    |
//| status          | running / success / fail |
//| input           | JSON                     |
//| output          | JSON                     |
//| create_time     | 创建时间                     |
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "执行ID")
    private Long id;
    @TableField("conversation_id")
    @Schema(description = "会话ID")
    private String conversationId;
    @TableField("message_id")
    @Schema(description = "触发的消息ID")
    private Long messageId;
    @TableField("tool_id")
    @Schema(description = "调用的工具ID")
    private String toolId;
    @TableField("status")
    @Schema(description = "执行状态，running / success / fail")
    private String status;
    @TableField("input")
    @Schema(description = "工具输入，JSON格式")
    private String input;
    @TableField("output")
    @Schema(description = "工具输出，JSON格式")
    private String output;

}
