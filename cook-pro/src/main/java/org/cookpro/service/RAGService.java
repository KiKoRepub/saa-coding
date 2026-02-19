package org.cookpro.service;

import jakarta.annotation.Resource;
import org.cookpro.hooks.RAGMessagesHook;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RAGService {

    @Resource
    private VectorStore vectorStore;


    public RAGMessagesHook getRAGMessagesHook(){
        return new RAGMessagesHook(vectorStore);
    }




}
