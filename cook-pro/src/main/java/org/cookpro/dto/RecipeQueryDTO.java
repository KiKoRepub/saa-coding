package org.cookpro.dto;

import lombok.Data;

@Data
public class RecipeQueryDTO {

    private String query;
    private Integer topK = 5; // 默认返回前5条结果
    private Long userId;

}
