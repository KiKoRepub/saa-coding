package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Map;

@Data
public class ToolCallVo {

    @Schema(description = "工具名")
    private String toolName;
    @Schema(description = "工具调用时参数输入")
    private Map<String, Object> arguments;
    @Schema(description = "工具调用结果")
    private String result;

}
