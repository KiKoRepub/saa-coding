package org.quickstart.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.fastjson.util.BiFunction;
import org.quickstart.tool.WeatherTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {


    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeChatKey;

    @Bean
    public ReactAgent weatherAgent() {
        // 初始化 ChatModel
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashscopeChatKey)
                .build();

        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 定义天气查询工具
        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherTool())
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();

        // 创建 agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(weatherTool)
                .systemPrompt("You are a helpful assistant")
                .saver(new MemorySaver())
                .build();

        return agent;
    }


}
