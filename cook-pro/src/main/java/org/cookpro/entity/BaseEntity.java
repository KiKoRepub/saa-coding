package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BaseEntity {

    @TableField("deleted")
    @Schema(description = "逻辑删除字段，0表示未删除，1表示已删除", example = "0")
    private int deleted;
    @TableField("create_user")
    @Schema(description = "创建用户", example = "admin")
    private String createUser;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;


}
