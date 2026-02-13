package org.cookpro.node;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeActionWithConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QueryExpanderNode implements NodeActionWithConfig {

    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;

    public QueryExpanderNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplate = new PromptTemplate(
                """
                你是一个搜索优化专家。请为以下查询生成 {number} 个不同的变体。
                +
                原始查询：{query}
                 +
                查询变体：
                """
        );
    }

    @Override
    public Map<String, Object> apply(OverAllState state, RunnableConfig config) throws Exception {
// 获取输入参数
        String query = state.value("query", "").toString();
        Integer number = state.value("expanderNumber", 3);

// 调用 LLM
        String result = chatClient.prompt()
                .user(user -> user
                        .text(promptTemplate.getTemplate())
                        .param("query", query)
                        .param("number", number))
                .call()
                .content();

// 处理结果
        String[] variants = result.split("");

// 返回更新的状态
                Map<String, Object> output = new HashMap<>();
        output.put("queryVariants", Arrays.asList(variants));
        return output;
    }
}