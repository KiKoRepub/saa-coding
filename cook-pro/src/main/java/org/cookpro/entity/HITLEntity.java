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

 */
