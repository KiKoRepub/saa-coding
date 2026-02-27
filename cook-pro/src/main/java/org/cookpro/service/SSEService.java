package org.cookpro.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cookpro.entity.SSEUserRecord;
import org.cookpro.enums.SSEUserRecordStatusEnum;
import org.cookpro.mapper.SSEUserRecordMapper;
import org.cookpro.mq.MessageQueueManager;
import org.cookpro.mq.SSEQueueMessage;
import org.cookpro.sse.SSEServer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
public class SSEService  extends ServiceImpl<SSEUserRecordMapper,SSEUserRecord> {


    private static ObjectMapper objectMapper = new ObjectMapper();
    public String sendMessage(Long userId, Long toId, String eventName, Object data)  {
        try {
            SseEmitter userEmitter = getUserEmitter(toId);

            SSEUserRecordStatusEnum result;

            if (userEmitter == null) {
                // 用户未连接SSE，存入阻塞队列
                SSEQueueMessage message = new SSEQueueMessage(userId, toId, eventName, data);
                MessageQueueManager.addMessageToQueue(message);
                result = SSEUserRecordStatusEnum.WAITING; // 未实时发送，等待用户连接后发送
            } else {
                // 实时发送消息
                userEmitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                result = SSEUserRecordStatusEnum.SUBMITTED;
            }
            // 保存消息记录（原有逻辑）
            saveUserMessageRecord(userId, toId, eventName, data, result.description);

            return result.description;
        }catch (IOException | InterruptedException e){
            log.error("发送SSE消息失败: " + e.getMessage());
            return SSEUserRecordStatusEnum.ERROR.description;
        }
    }

    private void saveUserMessageRecord(Long userId, Long toId, String eventName, Object data,String status){
        try {
            SSEUserRecord record = new SSEUserRecord();
            record.setUserId(userId);
            record.setToId(toId);
            record.setData(objectMapper.writeValueAsString(data));
            record.setEventName(eventName);
            record.setStatus(status);

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

        // 更新数据库中对应的消息记录状态

        QueryWrapper<SSEUserRecord> queryWrapper = new QueryWrapper<SSEUserRecord>()
                .eq("to_id", toId)
                .eq("status", SSEUserRecordStatusEnum.WAITING.description);

        List<SSEUserRecord> userRecords = list(queryWrapper);

        for (SSEUserRecord userRecord : userRecords) {
            userRecord.setStatus(SSEUserRecordStatusEnum.SUBMITTED.description);
        }

        updateBatchById(userRecords);

    }

}
