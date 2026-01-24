package org.quickstart.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.Optional;
import java.util.function.BiFunction;

import static com.alibaba.cloud.ai.graph.agent.tools.ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY;

public class UserLocationTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
            @ToolParam(description = "User query") String query,
            ToolContext toolContext) {
// 从上下文中获取用户信息
        String userId = "";
        if (toolContext != null && toolContext.getContext() != null) {
            RunnableConfig runnableConfig = (RunnableConfig) toolContext.getContext().get(AGENT_CONFIG_CONTEXT_KEY);
            Optional<Object> userIdObjOptional = runnableConfig.metadata("user_id");
            if (userIdObjOptional.isPresent()) {
                userId = (String) userIdObjOptional.get();
            }
        }
        if (userId == null) {
            userId = "1";
        }
        return "1".equals(userId) ? "Florida" : "San Francisco";
    }
}
//
//    // 创建工具回调
//    ToolCallback getWeatherTool = FunctionToolCallback
//            .builder("getWeatherForLocation", new WeatherForLocationTool())
//            .description("Get weather for a given city")
//            .inputType(String.class)
//            .build();
//
//    ToolCallback getUserLocationTool = FunctionToolCallback
//            .builder("getUserLocation", new UserLocationTool())
//            .description("Retrieve user location based on user ID")
//            .inputType(String.class)
//            .build();
//
//}
