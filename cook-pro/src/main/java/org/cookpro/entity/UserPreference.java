package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPreference extends BaseEntity{
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;


    @TableField("prefer_content")
    @Schema(description = "用户偏好内容，json字符串")
    private String preferContent;



}
