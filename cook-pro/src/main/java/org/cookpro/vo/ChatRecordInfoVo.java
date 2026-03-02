package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChatRecordInfoVo {

        @Schema(description = "用户消息")
        private String userMessage;

        @Schema(description = "助手回复")
        private String botResponse;

        @Schema(description = "工具调用信息")
        private List<ToolCallVo> toolCalls;

        @Schema(description = "记录创建时间")
        private String createTime;

}
