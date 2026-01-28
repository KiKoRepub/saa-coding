package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.cookpro.handler.LinkedListStringTypeHandler;

import java.util.LinkedList;
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

    @TableField(value = "steps",typeHandler = LinkedListStringTypeHandler.class)
    @Schema(description = "步骤")
    private LinkedList<String> steps; // 使用LinkedList以保持步骤的顺序

    @TableField("image_url")
    @Schema(description = "图片链接")
    private String imageUrl;



}
/*
CREATE TABLE `recipe` (
  `id`              BIGINT        NOT NULL COMMENT '菜谱ID',
  `dash_name`       VARCHAR(255)  DEFAULT NULL COMMENT '菜名',
  `ingredients`     VARCHAR(1024) DEFAULT NULL COMMENT '食材',
  `toppings`        VARCHAR(1024) DEFAULT NULL COMMENT '调料',
  `steps`           JSON          DEFAULT NULL COMMENT '步骤（JSON格式存储有序步骤列表）',
  `image_url`       VARCHAR(512)  DEFAULT NULL COMMENT '图片链接',
  `deleted`         TINYINT       NOT NULL DEFAULT '0' COMMENT '逻辑删除字段，0表示未删除，1表示已删除',
  `create_time`     DATETIME      DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜谱表';
 */