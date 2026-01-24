package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import org.cookpro.exception.ChatException;
import org.cookpro.hooks.RAGMessagesHook;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag")
public class RAGController {

    @Resource
    DashScopeChatModel chatModel;

    @Resource
    VectorStore vectorStore;

    @GetMapping("/test")
    public String testRAG(){
        // 创建带有 RAG Hook 的 Agent
        ReactAgent ragAgent = ReactAgent.builder()
                .name("rag_agent")
                .model(chatModel)
                .hooks(new RAGMessagesHook(vectorStore))
                .build();

        // 调用 Agent
        try {
            AssistantMessage response = ragAgent.call("Spring AI Alibaba支持哪些模型？");
            System.out.println("答案: " + response.getText());
        }catch (Exception e){
            throw new ChatException("RAG 聊天失败: " + e.getMessage(), e);
        }

    }


}
