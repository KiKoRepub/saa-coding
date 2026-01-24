package org.quickstart.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.BiFunction;

    // 天气查询工具
public class WeatherForLocationTool implements BiFunction<String, ToolContext, String> {
        @Override
        public String apply(
                @ToolParam(description = "The city name") String city,
                ToolContext toolContext) {
            return "It's always sunny in " + city + "!";
        }
    }

    // 用户位置工具 - 使用上下文
