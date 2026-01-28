package org.cookpro.tools;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.json.JacksonJsonParser;

import java.util.List;

public class AgenticRAGSearchTool {


    private final VectorStore vectorStore;


    public AgenticRAGSearchTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }


    @Tool(description = "Perform a similarity search in the vector store")
    public String similaritySearch(@ToolParam(description = "Search query keyword") String query,
                                   @ToolParam(required = false,description = "how many result you want,default 5") int k) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(k)
                .build();
        try {
            List<Document> documentList = vectorStore.similaritySearch(searchRequest);
            return JSONUtil.toJsonStr(documentList);
        } catch (Exception e) {
            return "Error performing similarity search: " + e.getMessage();
        }
    }


}
