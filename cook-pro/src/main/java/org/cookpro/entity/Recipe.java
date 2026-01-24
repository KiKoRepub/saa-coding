package org.cookpro.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Recipe {

    @Schema(description = "菜名")
    private String dashName;

    @Schema(description = "食材")
    private String ingredients;

    @Schema(description = "调料")
    private String toppings;


    @Schema(description = "步骤")
    private LinkedList<String> steps; // 使用LinkedList以保持步骤的顺序


    @Schema(description = "图片链接")
    private String imageUrl;



}
