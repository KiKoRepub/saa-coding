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
/*
CREATE TABLE `user_preference` (
  `id`              BIGINT       NOT NULL COMMENT '主键ID',
  `user_id`         BIGINT       DEFAULT NULL COMMENT '用户ID',
  `prefer_content`  TEXT         DEFAULT NULL COMMENT '用户偏好内容，json字符串',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_user`     VARCHAR(255) DEFAULT NULL COMMENT '创建用户',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 可选：添加用户ID索引，提升关联查询效率
  INDEX idx_user_preference_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';
 */