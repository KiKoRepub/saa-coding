package org.cookpro.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.cookpro.entity.ChatRecord;
import org.cookpro.mapper.ChatRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRecordService extends ServiceImpl<ChatRecordMapper, ChatRecord> {

    @Resource
    ChatRecordMapper chatRecordMapper;

     public Integer saveBatch(List<ChatRecord> chatRecords) {
        return chatRecordMapper.batchInsert(chatRecords);
    }

    public List<ChatRecord> listByConversationId(String conversationId) {
        return lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByAsc(ChatRecord::getCreatedAt)
                .list();
    }

    public List<ChatRecord> listByUserId(Long userId){
        return lambdaQuery()
                .eq(ChatRecord::getUserId, userId)
                .orderByAsc(ChatRecord::getCreatedAt)
                .list();
    }

}
