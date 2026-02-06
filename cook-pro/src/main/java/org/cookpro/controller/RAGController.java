package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.dto.RecipeAddDTO;
import org.cookpro.exception.ChatException;
import org.cookpro.hooks.RAGMessagesHook;
import org.cookpro.service.RecipeRAGService;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;

@RestController
@RequestMapping("/rag")
public class RAGController {

    @Resource
    DashScopeChatModel chatModel;

    @Resource
    VectorStore vectorStore;

    @Resource
    RecipeRAGService recipeRAGService;




    @PostMapping(value = "/addRecipe")
    public R<String> addRAGRecipe(RecipeAddDTO dto) throws IOException {
        return R.ok(recipeRAGService.addVectorRecipe(dto));
    }

    @PostMapping(value = "/addRecipesFromFile")
    @Operation(summary = "从指定的 JSON 文件中批量添加菜谱到向量数据库", description = "通过提供的 JSON 文件路径，批量添加菜谱到向量数据库中")
    public R<String> addRAGRecipesFromFile(@RequestParam("jsonFilePath") String jsonFilePath) throws IOException {
        return R.ok(recipeRAGService.addVectorRecipe(jsonFilePath));
    }

    @PostMapping(value = "/addRecipes")
    public R<String> addRAGRecipes() throws IOException {
        return R.ok(recipeRAGService.addVectorRecipe("/recipes.json"));
    }

}
