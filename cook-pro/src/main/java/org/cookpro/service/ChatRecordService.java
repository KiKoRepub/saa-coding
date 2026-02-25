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



}
