package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


public class BaseEntity {

    @TableField("deleted")
    @Schema(description = "逻辑删除字段，0表示未删除，1表示已删除", example = "0")
    public int deleted;
    @TableField("create_time")
    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    public LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    public LocalDateTime updateTime;


}
