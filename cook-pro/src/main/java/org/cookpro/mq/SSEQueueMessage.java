package org.cookpro.mq;

import lombok.Data;

@Data
public class SSEQueueMessage {
    private Long userId;      // 发送者ID
    private Long toId;        // 接收者ID
    private String eventName; // 事件名称
    private Object data;      // 消息数据

    // 构造方法
    public SSEQueueMessage(Long userId, Long toId, String eventName, Object data) {
        this.userId = userId;
        this.toId = toId;
        this.eventName = eventName;
        this.data = data;
    }

}