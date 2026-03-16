package org.cookpro.config;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import jakarta.annotation.Resource;
import org.cookpro.config.properties.ToolEnvProperties;
import org.cookpro.hooks.ToolNoticeHook;
import org.cookpro.tools.WebSearchTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AgentModelConfig {

    @Resource
    WebSearchTool webSearchTool;
    @Bean
    @Primary
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

    @Resource
    ToolNoticeHook toolNoticeHook; // 引入 调用工具的 通知Hook
    @Bean
    public ReactAgent dashscopeHITLAgent(DashScopeChatModel chatModel,
                                     ToolEnvProperties toolEnvProperties) {



        MemorySaver memorySaver = new MemorySaver();


// 创建人工介入Hook
        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("google_web_search", ToolConfig.builder()
                        .description("谷歌联网操作需要审批")
                        .build())
                .approvalOn("boCha_web_search", ToolConfig.builder()
                        .description("博查联网操作需要审批")
                        .build())
                .build();


        ToolCallback[] webSearchToolCallbacks = ToolCallbacks.from(
               webSearchTool
        );


        return ReactAgent.builder()
                .name("human_in_loop_agent")
                .model(chatModel)
                .tools(webSearchToolCallbacks)
                .hooks(humanInTheLoopHook,toolNoticeHook)
                .saver(memorySaver)
                .build();
    }
}
