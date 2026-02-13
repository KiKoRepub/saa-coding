package org.cookpro.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cookpro.entity.SSEUserRecord;
import org.cookpro.mapper.SSEUserRecordMapper;
import org.cookpro.mq.MessageQueueManager;
import org.cookpro.mq.SSEQueueMessage;
import org.cookpro.sse.SSEServer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class SSEService  extends ServiceImpl<SSEUserRecordMapper,SSEUserRecord> {


    private static ObjectMapper objectMapper = new ObjectMapper();
    public boolean sendMessage(Long userId, Long toId, String eventName, Object data) throws IOException, InterruptedException {
        SseEmitter userEmitter = getUserEmitter(toId);

        boolean result;

        if (userEmitter == null) {
            // 用户未连接SSE，存入阻塞队列
            SSEQueueMessage message = new SSEQueueMessage(userId, toId, eventName, data);
            MessageQueueManager.addMessageToQueue(message);
            result = false; // 未实时发送，返回false
        } else {
            // 实时发送消息
            userEmitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            result = true;
        }
        // 保存消息记录（原有逻辑）
        saveUserMessageRecord(userId, toId, eventName, data,result);

        return result;
    }

    private void saveUserMessageRecord(Long userId, Long toId, String eventName, Object data,boolean submitted) {
        try {
            SSEUserRecord record = new SSEUserRecord();
            record.setUserId(userId);
            record.setToId(toId);
            record.setData(objectMapper.writeValueAsString(data));
            record.setEventName(eventName);
            record.setSubmitted(submitted);

            save(record);



        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
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
