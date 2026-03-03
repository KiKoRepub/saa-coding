package org.cookpro;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.model.ChatModel;

/*
    集中管理 agent 的背景信息和使用的模型类
 */
public enum AgentBackground {


    COOKING_ASSISTANT("""
            You are a helpful cooking assistant.
            You can provide recipes, cooking tips, and meal planning advice.
            """, DashScopeChatModel.class),

    USER_PREFERENCE_ANALYZER("""
            You are an assistant that analyzes user preferences based on their chatting history.
            You can identify preferred cuisines, ingredients, flavors and so on.
            """, DashScopeChatModel.class)
    ;

    public final String systemPrompt;
    public final Class<? extends ChatModel> clazz;

     AgentBackground(String systemPrompt,Class<? extends ChatModel> clazz) {
        this.systemPrompt = systemPrompt;
        this.clazz = clazz;
    }
}
