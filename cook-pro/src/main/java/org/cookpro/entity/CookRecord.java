package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("cook_record")
@EqualsAndHashCode(callSuper = true)
public class CookRecord extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("recipe_id")
    @Schema(description = "菜谱ID")
    private String recipeId;

    @TableField("product_url")
    @Schema(description = "成品图片URL")
    private String productUrl;

    @TableField("description")
    @Schema(description = "备注")
    private String remark;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private String createTime;

}

/*
CREATE TABLE `cook_record` (
  `id`              BIGINT       NOT NULL COMMENT '主键ID',
  `user_id`         BIGINT       DEFAULT NULL COMMENT '用户ID',
  `recipe_id`       VARCHAR(255) DEFAULT NULL COMMENT '菜谱ID',
  `product_url`     VARCHAR(512) DEFAULT NULL COMMENT '成品图片URL',
  `remark`          VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='烹饪记录表';
 */