package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.cookpro.dto.ChatRecordAddDTO;
import org.cookpro.dto.TimelineNode;
import org.cookpro.entity.ChatRecord;
import org.cookpro.enums.ChatRecordTypeEnum;
import org.cookpro.mapper.ChatRecordMapper;
import org.cookpro.utils.DateUtils;
import org.cookpro.utils.ToolUtils;
import org.cookpro.vo.ChatRecordInfoVo;
import org.cookpro.vo.ToolCallVo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatRecordService extends ServiceImpl<ChatRecordMapper, ChatRecord> {

    @Resource
    ChatRecordMapper chatRecordMapper;

    public Integer saveBatch(List<ChatRecord> chatRecords) {
        return chatRecordMapper.batchInsert(chatRecords);
    }

    public List<ChatRecordInfoVo> listByConversationId(String conversationId) {

            List<ChatRecord> records = chatRecordMapper.selectByConversationWithOrder(conversationId);
            // 获得了同一会话中
            // 属于同一线程的记录

            if (records.isEmpty()){
                return new ArrayList<>();
            }


        return mergeCommonThreadRecords(records);
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

    public List<TimelineNode> getTimeline(String threadId) {

        List<ChatRecord> records =
                chatRecordMapper.selectList(
                        new LambdaQueryWrapper<ChatRecord>()
                                .eq(ChatRecord::getThreadId, threadId)
                                .orderByAsc(ChatRecord::getCreateTime)
                );

        return records.stream()
                .map(this::convert)
                .toList();
    }
    private List<ChatRecordInfoVo> mergeCommonThreadRecords(List<ChatRecord> records) {
        List<ChatRecordInfoVo> result = new ArrayList<>();
        // 维护每个 Assistant 消息当前的工具填充进度
        Map<Long, ChatRecordInfoVo> assistantMap = new HashMap<>();
        Map<Long, Integer> assistantToolIndexMap = new HashMap<>();

        for (ChatRecord record : records) {
            String recordType = record.getRecordType();

            // 1. 处理用户消息
            if (ChatRecordTypeEnum.USER.description.equals(recordType)) {

                ChatRecordInfoVo userMsg = new ChatRecordInfoVo();
                userMsg.setContent(record.getUserMessage());

                result.add(userMsg);
            }

            // 2. 处理 AI 消息主体
            else if (ChatRecordTypeEnum.ASSISTANT.description.equals(recordType)) {

                ChatRecordInfoVo assistantMsg = buildBasicVo(record, recordType);

                assistantMsg.setContent(record.getBotResponse());
                assistantMsg.setToolCalls(new ArrayList<>());


                result.add(assistantMsg);
                assistantMap.put(record.getId(), assistantMsg);
                assistantToolIndexMap.put(record.getId(), 0); // 初始化该消息的工具索引
            }

            // 3. 处理工具调用
            else if (ChatRecordTypeEnum.TOOL_CALL.description.equals(recordType)) {
                ChatRecordInfoVo parent = assistantMap.get(record.getParentId());
                if (parent != null) {

                    ToolCallVo toolView = new ToolCallVo();
                    // 填充
                    toolView.setToolName(ToolUtils.extractToolName(record.getToolCall()));
                    toolView.setArguments(ToolUtils.extractToolArguments(record.getToolCall()));

                    parent.getToolCalls().add(toolView);
                }
            }

            // 4. 处理工具结果
            else if (ChatRecordTypeEnum.TOOL_RESULT.description.equals(recordType)) {
                Long parentId = record.getParentId();
                ChatRecordInfoVo parentMsg = assistantMap.get(parentId);

                if (parentMsg != null && !parentMsg.getToolCalls().isEmpty()) {
                    int currentIndex = assistantToolIndexMap.get(parentId);

                    if (currentIndex < parentMsg.getToolCalls().size()) {
                        ToolCallVo targetTool = parentMsg.getToolCalls().get(currentIndex);
                        // 将工具结果填充到对应的工具调用中
                        targetTool.setResult(record.getToolResult());
                        // 递增该特定 Assistant 的索引
                        assistantToolIndexMap.put(parentId, currentIndex + 1);
                    }
                }
            }

            // 5. 处理审核状态
            else if (ChatRecordTypeEnum.AUDIT.description.equals(recordType)) {
                ChatRecordInfoVo auditParent = assistantMap.get(record.getParentId());
                if (auditParent != null) {
                    auditParent.setAudit(record.getHitlStatus(), record.getUserMessage());
                }
            }
            else if (ChatRecordTypeEnum.ASSISTANT_FINAL.description.equals(recordType)){
                ChatRecordInfoVo assistantMsg = new ChatRecordInfoVo();
                assistantMsg.setRecordType(recordType);
                assistantMsg.setContent(record.getBotResponse());
                assistantMsg.setToolCalls(new ArrayList<>());
                assistantMsg.setCreateTime(DateUtils.formatTime(record.getCreateTime()));

                result.add(assistantMsg);
                // 不再接受工具调用和审核信息
            }
        }
        return result;
    }
    private TimelineNode convert(ChatRecord record) {
        String recordType = record.getRecordType();
        // 默认展示用户消息
        String content = record.getUserMessage();

        if (ChatRecordTypeEnum.ASSISTANT.description.equals(recordType)) {
            content = record.getBotResponse();
        }else if (ChatRecordTypeEnum.TOOL_RESULT.description.equals(recordType)) {
            content = record.getToolResult();
        }


        return TimelineNode.builder()
                .recordId(record.getId())
                .threadId(record.getThreadId())
                .recordType(record.getRecordType())
                .content(content)
                .hitlStatus(record.getHitlStatus())
                .time(record.getCreateTime())
                .build();
    }

    private ChatRecordInfoVo buildBasicVo(ChatRecord record, String type) {
        ChatRecordInfoVo vo = new ChatRecordInfoVo();
        vo.setThreadId(record.getThreadId());
        vo.setRecordType(type);
        vo.setCreateTime(DateUtils.formatTime(record.getCreateTime()));
        return vo;
    }

}
