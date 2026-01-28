package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/rag")
public class RAGController {

    @Resource
    DashScopeChatModel chatModel;

    @Resource
    VectorStore vectorStore;

    @Resource
    RecipeRAGService recipeRAGService;

    @GetMapping("/test")
    public void testRAG(){
        // 在 agent 执行前 查询向量数据库，获取相关文档，并将其添加到消息中
        // 创建带有 RAG Hook 的 Agent
        ReactAgent ragAgent = ReactAgent.builder()
                .name("rag_agent")
                .model(chatModel)
                .hooks(new RAGMessagesHook(vectorStore))
                .build();

        // 调用 Agent
        try {
            AssistantMessage response = ragAgent.call("番茄炒蛋应该怎么制作？");
            System.out.println("答案: " + response.getText());
        }catch (Exception e){
            throw new ChatException("RAG 聊天失败: " + e.getMessage(), e);
        }

    }


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
