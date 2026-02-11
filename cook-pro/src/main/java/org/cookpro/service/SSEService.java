package org.cookpro.service;

import org.cookpro.mq.MessageQueueManager;
import org.cookpro.mq.SSEQueueMessage;
import org.cookpro.sse.SSEServer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class SSEService {



    public boolean sendMessage(Long userId, Long toId, String eventName, Object data) throws IOException, InterruptedException {
        SseEmitter userEmitter = getUserEmitter(toId);

        // 保存消息记录（原有逻辑）
        saveUserMessageRecord(userId, toId, eventName, data);

        if (userEmitter == null) {
            // 用户未连接SSE，存入阻塞队列
            SSEQueueMessage message = new SSEQueueMessage(userId, toId, eventName, data);
            MessageQueueManager.addMessageToQueue(message);
            return false; // 未实时发送，返回false
        } else {
            // 实时发送消息
            userEmitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            return true;
        }
    }

    private void saveUserMessageRecord(Long userId, Long toId, String eventName, Object data) {

    }



    private SseEmitter getUserEmitter(Long userId) {
        if (SSEServer.getEmitter(userId.toString()) == null){
            return SSEServer.connect(userId);
        }
        return SSEServer.getEmitter(userId.toString());
    }

    public void onUserConnect(Long toId, SseEmitter emitter) {
        // 消费队列中的积压消息
        MessageQueueManager.consumeMessageQueue(toId, emitter);
    }

}
