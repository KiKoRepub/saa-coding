package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.cookpro.dto.ChatRecordAddDTO;
import org.cookpro.entity.ChatRecord;
import org.cookpro.mapper.ChatRecordMapper;
import org.cookpro.utils.ToolUtils;
import org.cookpro.vo.ChatRecordInfoVo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatRecordService extends ServiceImpl<ChatRecordMapper, ChatRecord> {

    @Resource
    ChatRecordMapper chatRecordMapper;

    public Integer saveBatch(List<ChatRecord> chatRecords) {
        return chatRecordMapper.batchInsert(chatRecords);
    }

    public List<ChatRecordInfoVo> listByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatRecord> queryWrapper = new LambdaQueryWrapper<ChatRecord>()
                .eq(ChatRecord::getConversationId, conversationId)
                .eq(ChatRecord::getDeleted, 0)
                .orderByAsc(ChatRecord::getCreateTime);

        List<ChatRecord> chatRecords = list(queryWrapper);

        return chatRecords.stream()
                .map(this::toInfoVo)
                .toList();


    }

    private ChatRecordInfoVo toInfoVo(ChatRecord chatRecord) {
        ChatRecordInfoVo vo = new ChatRecordInfoVo();
        BeanUtil.copyProperties(chatRecord, vo);

        vo.setToolCalls(ToolUtils.serializeToolCalls(chatRecord.getToolCall()));


        return vo;

    }

    public List<ChatRecord> listByUserId(Long userId){
        return lambdaQuery()
                .eq(ChatRecord::getUserId, userId)
                .orderByAsc(ChatRecord::getCreateTime)
                .list();
    }
    public String saveOneRecord(ChatRecordAddDTO dto){
        ChatRecord record = new ChatRecord();

        BeanUtil.copyProperties(dto, record);

        save(record);

        return record.getId().toString();
    }

}
