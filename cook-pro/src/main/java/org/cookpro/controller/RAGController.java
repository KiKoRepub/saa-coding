package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.dto.RecipeAddDTO;
import org.cookpro.dto.RecipeQueryDTO;
import org.cookpro.exception.ChatException;
import org.cookpro.service.RecipeRAGService;
import org.cookpro.vo.RecipeQueryVo;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag")
public class RAGController {


    @Resource
    RecipeRAGService recipeRAGService;


    @PostMapping("/query")
    @Operation(summary = "查询菜谱", description = "根据用户输入的查询内容，返回相关的菜谱信息")
    public R<List<RecipeQueryVo>> queryRAGRecipe(@RequestBody RecipeQueryDTO dto) throws ChatException {
        return R.ok(recipeRAGService.queryRecipe(dto));
    }

    @GetMapping("/recommand")
    @Operation(summary = "推荐菜谱", description = "根据用户输入的查询内容，推荐相关的菜谱信息")
    public R<List<RecipeQueryVo>> recommandRAGRecipe(@RequestParam("id")Long userId){
        return R.ok(recipeRAGService.recommendRecipe(userId));
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

/*

 */

}
