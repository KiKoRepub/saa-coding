package org.cookpro.config;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import org.cookpro.config.properties.ToolEnvProperties;
import org.cookpro.tools.WebSearchTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentModelConfig {


    @Bean
    public DashScopeChatModel dashScopeChatModel(DashScopeConnectionProperties connectionProperties){
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(connectionProperties.getApiKey())
                .baseUrl(connectionProperties.getBaseUrl())
                .build();

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        return chatModel;
    }

    @Bean
    public ReactAgent dashscopeHITLAgent(DashScopeChatModel chatModel,
                                     ToolEnvProperties toolEnvProperties) {



        MemorySaver memorySaver = new MemorySaver();


// 创建人工介入Hook
        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder() // [!code highlight]
                .approvalOn("google_web_search", ToolConfig.builder() // [!code highlight]
                        .description("谷歌联网操作需要审批") // [!code highlight]
                        .build()) // [!code highlight]
                .approvalOn("bocha_web_search", ToolConfig.builder() // [!code highlight]
                        .description("博查联网操作需要审批") // [!code highlight]
                        .build()) // [!code highlight]
                .build(); // [!code highlight]


        ToolCallback[] webSearchToolCallbacks = ToolCallbacks.from(
                new WebSearchTool(toolEnvProperties.getGoogleWebSearchApiKey())
        );


        return ReactAgent.builder()
                .name("human_in_loop_agent")
                .model(chatModel)
                .tools(webSearchToolCallbacks)
                .hooks(humanInTheLoopHook)
                .saver(memorySaver)
                .build();
    }
}
