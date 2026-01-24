package org.cookpro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.cookpro.entity.Recipe;
import org.cookpro.utils.VectorStoreUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RecipeService {


    @Resource
    private VectorStore vectorStore;

    private static final ObjectMapper  objectMapper = new ObjectMapper();

    // RecipeService.java
    public String toSearchableText(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("菜名：").append(recipe.getDashName()).append("\n");
        sb.append("食材：").append(recipe.getIngredients()).append("\n");
        sb.append("调料：").append(recipe.getToppings()).append("\n");
        sb.append("步骤：\n");
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            sb.append((i + 1)).append(". ").append(recipe.getSteps().get(i)).append("\n");
        }
        return sb.toString().trim();
    }


    public void addVectorRecipe(String filePath) throws IOException {
        // 从 classpath 加载 recipes.json
        InputStream is = getClass().getResourceAsStream(filePath);
        List<Recipe> recipes = Arrays.asList(
                objectMapper.readValue(is, Recipe[].class)
        );

        List<Document> documents = recipes.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents); // 自动嵌入并存储
    }


    public void addVectorRecipe(List<Recipe> recipes) throws IOException {
        List<Document> documents = recipes.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents); // 自动嵌入并存储
    }



    public Document toDocument(Recipe recipe){
        String content = toSearchableText(recipe);
        Map<String, Object> metadata = Map.of(
                "dishName", recipe.getDashName(),
                "imageUrl", recipe.getImageUrl()
        );
        return new Document(content, metadata);
    }
}
