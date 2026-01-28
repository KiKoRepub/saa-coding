package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @TableId(type =IdType.ASSIGN_ID)
    @Schema(description = "用户ID")
    private Long id;

    @TableField("username")
    @Schema(description = "用户名")
    private String username;
    @TableField("password")
    @Schema(description = "密码")
    private String password;
    @TableField("role")
    @Schema(description = "用户角色")
    private String role;

}
/*
CREATE TABLE `user` (
  `id`              BIGINT       NOT NULL COMMENT '主键ID',
  `username`        VARCHAR(255) DEFAULT NULL COMMENT '用户名',
  `password`        VARCHAR(255) DEFAULT NULL COMMENT '密码',
  `role`            VARCHAR(255) DEFAULT NULL COMMENT '用户角色',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
 */