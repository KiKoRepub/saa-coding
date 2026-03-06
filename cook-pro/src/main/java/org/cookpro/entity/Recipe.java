package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cookpro.handler.ListStringTypeHandler;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Recipe extends BaseEntity{

    @TableId
    @Schema(description = "菜谱ID")
    private Long id;

    @TableField("dash_name")
    @Schema(description = "菜名")
    private String dashName;

    @TableField("ingredients")
    @Schema(description = "食材")
    private String ingredients;

    @TableField("toppings")
    @Schema(description = "调料")
    private String toppings;

    @TableField(value = "steps",typeHandler = ListStringTypeHandler.class)
    @Schema(description = "步骤")
    private List<String> steps; // 使用LinkedList以保持步骤的顺序

    @TableField("image_url")
    @Schema(description = "图片链接")
    private String imageUrl;



}
/*
CREATE TABLE `recipe` (
  `id`              BIGINT       NOT NULL COMMENT '菜谱ID',
  `dash_name`       VARCHAR(255) DEFAULT NULL COMMENT '菜名',
  `ingredients`     TEXT         DEFAULT NULL COMMENT '食材',
  `toppings`        TEXT         DEFAULT NULL COMMENT '调料',
  `steps`           TEXT         DEFAULT NULL COMMENT '步骤（JSON数组格式存储）',
  `image_url`       VARCHAR(512) DEFAULT NULL COMMENT '图片链接',
  `deleted`         TINYINT      NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_user`     VARCHAR(255) DEFAULT NULL COMMENT '创建用户',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  -- 可选：按菜名检索的索引，提升查询效率
  INDEX idx_recipe_dash_name (`dash_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜谱表';
 */