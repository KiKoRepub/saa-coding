package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatRecordInfoVo {
        @Schema(description = "线程id")
        private String threadId;
        @Schema(description = "记录类型")
        private String recordType; // user / assistant
        @Schema(description = "内容，用户消息或助手回复")
        private String content;
        @Schema(description = "工具调用信息")
        private List<ToolCallVo> toolCalls;
        @Schema(description = "HITL审核状态，审核通过、待审核、审核拒绝等")
        private AuditView audit;
        @Schema(description = "记录创建时间")
        private String createTime;


    public static class AuditView {

        private String status;

        private String comment;
        public AuditView(String status, String comment) {
            this.status = status;
            this.comment = comment;
        }
    }
    public void setAudit(String status, String comment) {
        this.audit =  new AuditView(status, comment);
    }
}
