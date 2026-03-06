package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.cookpro.dto.RecipeAddDTO;
import org.cookpro.dto.RecipeQueryDTO;
import org.cookpro.dto.UserPreferenceDTO;
import org.cookpro.entity.Recipe;
import org.cookpro.vo.RecipeQueryVo;
import org.cookpro.vo.UserPreferenceVo;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
@Service
public class RecipeRAGService{


    @Resource
    private VectorStore vectorStore;
    @Resource
    private UserPreferenceService userPreferenceService;

    private static final ObjectMapper  objectMapper = new ObjectMapper();

    public List<RecipeQueryVo> queryRecipe(RecipeQueryDTO dto) {
        String query = dto.getQuery();
        List<Document> documentList = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(dto.getTopK())
                .filterExpression("user_id = " + dto.getUserId())
                .build());

        return documentList.stream()
                .map(this::toQueryVo)
                .toList();
    }


    // RecipeService.java

    public String addVectorRecipe(RecipeAddDTO dto) throws IOException {

        Recipe recipe = new Recipe();

        BeanUtil.copyProperties(dto,recipe);

        Document document = toDocument(recipe);
        vectorStore.add(Collections.singletonList(document)); // 自动嵌入并存储

        return "成功添加菜谱到向量数据库: " + recipe.getDashName();
    }

    public String addVectorRecipe(String jsonFilePath) throws IOException {
        // 从 classpath 加载 recipes.json

        InputStream is = getClass().getResourceAsStream(jsonFilePath);

        if (is == null) {
           is = tryToLoadFromUrl(jsonFilePath);
           if (is == null){
                throw new IOException("无法加载资源文件: " + jsonFilePath);
           }
        }


        List<Recipe> recipes = Collections.singletonList(objectMapper.readValue(is, Recipe.class));

        return addVectorRecipe(recipes);
    }




    public String addVectorRecipe(List<Recipe> recipes){
        List<Document> documents = recipes.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents); // 自动嵌入并存储


        return "成功添加 " + recipes.size() + " 个菜谱到向量数据库。";
    }

    public List<RecipeQueryVo> recommendRecipe(Long userId) {
        UserPreferenceVo preference = userPreferenceService.getPreference(userId);

        String preferContent = preference.getPreferContent();// 这里可以根据用户偏好构建查询条件，进行向量搜索推荐菜谱

        List<Document> documentList = vectorStore.similaritySearch(SearchRequest.builder()
                .query(preferContent)
                .topK(5) // 推荐前5个菜谱
                .filterExpression("user_id = " + userId) // 可选：根据用户ID过滤
                .build());
        return documentList.stream()
                .map(this::toQueryVo)
                .toList();
    }

    private Document toDocument(Recipe recipe){
        String content = toSearchableText(recipe);
        Map<String, Object> metadata = Map.of(
                "id", UUID.randomUUID(),
                "dishName", recipe.getDashName(),
                "imageUrl", recipe.getImageUrl(),
                "createUser", recipe.getCreateUser()
        );
        return new Document(content, metadata);
    }
    private String toSearchableText(Recipe recipe) {
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

    private InputStream tryToLoadFromUrl(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }

        try {
            // 支持 "http://", "https://", "file://", 甚至 classpath:（但 classpath 需特殊处理）
            URL url = new URI(filePath).toURL();
            URLConnection connection = url.openConnection();
            // 可选：设置超时防止卡死（尤其对 HTTP）
            if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
                connection.setConnectTimeout(5000); // 5秒连接超时
                connection.setReadTimeout(10000);    // 10秒读取超时
            }
            return connection.getInputStream();
        } catch (Exception e) {
            // 记录日志（如果使用了日志框架，比如 SLF4J）
            // log.warn("Failed to load resource from URL: " + filePath, e);
            return null; // “try”语义：失败返回 null，不抛异常
        }
    }
    private RecipeQueryVo toQueryVo(Document document) {
        RecipeQueryVo vo = new RecipeQueryVo();
        return vo;
    }

}
