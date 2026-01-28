package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.core.SpringProperties;

import java.util.List;

@Data
public class RecipeAddDTO {
    @Schema(description = "菜名", example = "番茄炒蛋")
    private String dashName;
    @Schema(description = "食材", example = "番茄, 鸡蛋, 盐, 油")
    private String ingredients;
    @Schema(description = "调料", example = "盐, 糖, 鸡精")
    private String toppings;
    @Schema(description = "步骤", example = "[\"切番茄\", \"打鸡蛋\", \"炒鸡蛋\", \"加番茄炒匀\"]")
    private List<String> steps;
    @Schema(description = "图片链接", example = "http://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "是否需要生成RAG", example = "true")
    private boolean needRAG;
}
