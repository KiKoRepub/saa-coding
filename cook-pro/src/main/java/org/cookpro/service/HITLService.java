package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.dto.HITLEditInfoDTO;
import org.cookpro.dto.HITLPageDTO;
import org.cookpro.dto.HITLReviewDTO;
import org.cookpro.dto.ToolChatDTO;
import org.cookpro.entity.HITLToolArgInfo;
import org.cookpro.entity.HITLEntity;
import org.cookpro.enums.HITLStatusEnum;
import org.cookpro.enums.SSEEventEnum;
import org.cookpro.mapper.HITLMapper;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.SystemPrinter;
import org.cookpro.vo.CommonEnumVo;
import org.cookpro.vo.HITLPageVo;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HITLService extends ServiceImpl<HITLMapper, HITLEntity> {
    //TODO 改为用户组装的 agent，包含用户选择的工具等信息
    @Resource
    ReactAgent dashscopeHITLAgent;

    @Resource
    ThreadPoolTaskExecutor asyncExecutor;

    @Resource
    SSEService sseService;
    @Resource
    ChatRecordService chatRecordService;
    @Resource
    MemoryCacheService memoryCacheService;
    @Resource
    ToolService toolService;

    public Page<HITLPageVo> getPublishPageList(HITLPageDTO dto) {
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();

        QueryWrapper<HITLEntity> wrapper = getPublishPageQueryWrapper(dto);

        return getPageVoList(pageNum, pageSize, wrapper);
    }
    public Page<HITLPageVo> getReviewPageList(HITLPageDTO dto) {
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();

        QueryWrapper<HITLEntity> wrapper = getReviewPageQueryWrapper(dto);

        return getPageVoList(pageNum, pageSize, wrapper);
    }

    public String reviewHitl(HITLReviewDTO dto) throws GraphRunnerException {
        Long id = dto.getId();


        HITLEntity hitlEntity = getById(id);

        if (! Objects.equals(hitlEntity.getStatus(), HITLStatusEnum.WAITING.description)){
            throw new IllegalStateException("该人工介入请求不处于待审核状态");
        }

        String threadId = hitlEntity.getThreadId();
        InterruptionMetadata interruptionMetadata = hitlEntity.getInterruptData();
        Long reviewerId = hitlEntity.getReviewerId();
        Long publisherId = hitlEntity.getPublisherId();

        boolean approved = dto.isApproved();

        // 获取 审核结果
        InterruptionMetadata approvalMetadata = getReviewResult(interruptionMetadata,approved);
        if (approved){
            sseService.sendMessage(reviewerId,publisherId,
                    SSEEventEnum.AUDIT_PASSED.eventName,
                    "您的人工介入请求已通过审核,正在恢复执行..."
            );
            hitlEntity.setStatus(HITLStatusEnum.APPROVED.description);
        }else {
            sseService.sendMessage(reviewerId,publisherId,
                    SSEEventEnum.AUDIT_REJECTED.eventName,
                    "您的人工介入请求未通过审核,原因是:"+ dto.getReviewComment()
            );
            hitlEntity.setStatus(HITLStatusEnum.REJECTED.description);
        }

        // 创建恢复配置
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                .build();
        // 创建异步任务 恢复 执行
        // 传入空消息，表示不需要发送新的输入，直接恢复执行
        createAsyncTask(null, resumeConfig,
                hitlEntity.getReviewerId(), hitlEntity.getPublisherId());

        // 保存审核结果
        updateById(hitlEntity);

        return hitlEntity.getId().toString();
    }

    public String editHitl(HITLEditInfoDTO dto) throws GraphRunnerException {

        Long id = dto.getId();
        HITLEntity hitlEntity = getById(id);

        if (! Objects.equals(hitlEntity.getStatus(), HITLStatusEnum.WAITING.description)){
            throw new IllegalStateException("该人工介入请求不处于待审核状态");
        }
        String threadId = hitlEntity.getThreadId();
        InterruptionMetadata interruptionMetadata = hitlEntity.getInterruptData();

        String message = dto.getMessage();

        List<HITLToolArgInfo> infoList = dto.getArgInfoList();
        String toolName = dto.getToolName();

        // 构建新的 InterruptionMetadata，修改特定工具的参数
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());


        List<InterruptionMetadata.ToolFeedback> toolFeedbackList = interruptionMetadata.toolFeedbacks();

        for (InterruptionMetadata.ToolFeedback toolFeedback : toolFeedbackList) {
            if (toolFeedback.getName().equals(toolName)){
                //
                builder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .arguments(HITLHelper.buildEditedArguments(infoList))
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.EDITED)
                                .build()
                );
            }else {
                builder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build()
                );
            }
        }

        InterruptionMetadata editMetaData = builder.build();

        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, editMetaData)
                .build();
        // 创建异步任务 恢复 执行
        createAsyncTask(message,
                resumeConfig,
                hitlEntity.getReviewerId(),
                hitlEntity.getPublisherId()
        );
        // 保存修改结果
        hitlEntity.setStatus(HITLStatusEnum.EDITED.description);
        updateById(hitlEntity);

        return hitlEntity.getId().toString();
    }


    public void initHITL(List<ToolChatDTO> toolChatDTOList, Long publisherId, String agentThreadId, InterruptionMetadata interruptionMetadata) {
        // 发生了中断
        // 获取中断的元信息，进行相应的处理（比如通知人工审核人员进行审核）
        HITLEntity hitlEntity = new HITLEntity();

        hitlEntity.setPublisherId(publisherId);

        hitlEntity.setInterruptData(interruptionMetadata);
        hitlEntity.setThreadId(agentThreadId);
        hitlEntity.setReason("等待人工审核工具调用");


        StringBuilder remarkBuilder = new StringBuilder();

        //只有一个 feedback
        Long auditorId = null;
        for (InterruptionMetadata.ToolFeedback feedback : interruptionMetadata.toolFeedbacks()) {
            remarkBuilder.append("工具: ").append(feedback.getName())
                    .append(", 参数: ").append(feedback.getArguments())
                    .append(", 描述: ").append(feedback.getDescription())
                    .append("\n");
            // 根据工具名称匹配审核人
            for (ToolChatDTO toolChatDTO : toolChatDTOList) {
                if (toolChatDTO.getToolName().equals(feedback.getName())) {
                    hitlEntity.setReviewerId(toolChatDTO.getAuditorId());
                    break;
                }
            }

        }
        hitlEntity.setRemark(remarkBuilder.toString());
        save(hitlEntity);

        //TODO 通知审核人 进行审核
        sseService.sendMessage(publisherId, auditorId, SSEEventEnum.WAITING_AUDIT.eventName,
                "您收到了一个新的人工审核请求，线程ID: " + agentThreadId + "，请尽快处理。");

    }
    private Page<HITLPageVo> getPageVoList(Integer pageNum, Integer pageSize, QueryWrapper<HITLEntity> wrapper) {
        Page<HITLEntity> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<HITLPageVo> voList = page.getRecords().stream()
                .map(this::toPageVo)
                .collect(Collectors.toList());

        Page<HITLPageVo> result = new Page<>(pageNum, pageSize, voList.size());
        result.setRecords(voList);
        return result;
    }

    private  QueryWrapper<HITLEntity> getPublishPageQueryWrapper(HITLPageDTO dto) {
        Long userId = 11L;
        return getBasePageQueryWrapper(dto).eq("publisher_id", userId);
    }
    private  QueryWrapper<HITLEntity> getReviewPageQueryWrapper(HITLPageDTO dto) {
        Long userId = 11L;
        return getBasePageQueryWrapper(dto).eq("review_id", userId);
    }
    private QueryWrapper<HITLEntity> getBasePageQueryWrapper(HITLPageDTO dto){
        QueryWrapper<HITLEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                .orderByDesc("create_time");
        //TODO 根据 dto 中的其他字段添加更多的查询条件，例如状态、时间范围等

        return queryWrapper;

    }

    private HITLPageVo toPageVo(HITLEntity entity) {
        HITLPageVo hitlPageVo = new HITLPageVo();

        BeanUtil.copyProperties(entity, hitlPageVo);

        return hitlPageVo;
    }

    private void createAsyncTask(String message, RunnableConfig resumeConfig,Long auditorId,Long publisherId) throws GraphRunnerException {
        CompletableFuture.runAsync(() -> {
            try {

                Flux<NodeOutput> finalFluxResult = dashscopeHITLAgent.stream(message, resumeConfig);

                String threadId = resumeConfig.threadId().get();
                Sinks.Many<String> sink = memoryCacheService.getInterruptSink(threadId);

                finalFluxResult.subscribe(output -> {
                    if (output instanceof InterruptionMetadata interruptionMetadata) {
                        // 初始化 中断信息
                        Long newAuditorId = null;
                        StringBuilder remarkBuilder = new StringBuilder();
                        for (InterruptionMetadata.ToolFeedback feedback : interruptionMetadata.toolFeedbacks()) {
                            String toolName = feedback.getName();

                            remarkBuilder.append("工具: ").append(toolName)
                                    .append(", 参数: ").append(feedback.getArguments())
                                    .append(", 描述: ").append(feedback.getDescription())
                                    .append("\n");

                            // 根据工具名称匹配审核人
                            ToolChatDTO chatDTO = toolService.getChatDTO(toolName);
                            newAuditorId = chatDTO.getAuditorId();

                        }
                        // 保存审核信息
                            HITLEntity hitlEntity = new HITLEntity();

                            hitlEntity.setPublisherId(publisherId);

                            hitlEntity.setInterruptData(interruptionMetadata);
                            hitlEntity.setThreadId(threadId);
                            hitlEntity.setReason("等待人工审核工具调用");

                            hitlEntity.setRemark(remarkBuilder.toString());
                            save(hitlEntity);

                            sseService.sendMessage(publisherId, newAuditorId, SSEEventEnum.WAITING_AUDIT.eventName,
                                    "您收到了一个新的人工审核请求，线程ID: " + threadId + "，请尽快处理。");
                        }

                        if (memoryCacheService.hasInterruptSink(threadId)) {
                            sink.tryEmitNext("[系统] 发生了工具调用中断，请等待人工审核结果...");
                        } else {
                            sink.tryEmitNext("[系统] 系统出现异常，无法处理人工审核，请稍后再试...");
                            sink.tryEmitComplete();
                        }
                    }
                , error -> {
                    log.error("恢复执行过程中发生错误", error);
                    if (sink != null) {
                        sink.tryEmitError(error);
                        memoryCacheService.removeInterruptSink(threadId);
                    }
                    sseService.sendMessage(auditorId, publisherId,
                            SSEEventEnum.ERROR.eventName,
                            "执行恢复过程中发生错误，原因是: " + error.getMessage());
                }, () -> {
                    if (sink != null) {
                        sink.tryEmitComplete();
                        memoryCacheService.removeInterruptSink(threadId);
                    }
                    sseService.sendMessage(auditorId, publisherId,
                            SSEEventEnum.COMPLETED.eventName,
                            "执行已完成。");
                });



            } catch (GraphRunnerException e) {
                log.error("异步恢复执行失败", e);

                sseService.sendMessage(auditorId, publisherId,
                    SSEEventEnum.ERROR.eventName,
                    "执行恢复失败，原因是: " + e.getMessage());
            }
        }, asyncExecutor);
    }


    private InterruptionMetadata getReviewResult(InterruptionMetadata interruptionMetadata,boolean isApproved) {

       InterruptionMetadata.ToolFeedback.FeedbackResult reviewResult = isApproved ? InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED
               : InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED;

        InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();

        toolFeedbacks.forEach(toolFeedback -> {
            InterruptionMetadata.ToolFeedback approvedFeedback =
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(reviewResult)
                            .build();
            feedbackBuilder.addToolFeedback(approvedFeedback);
        });

        return feedbackBuilder.build();
    }


    public List<HITLToolArgInfo> getToolArgInfo(Long id,String toolName) throws JsonProcessingException {
        HITLEntity entity = getById(id);

        InterruptionMetadata interruptData = entity.getInterruptData();

        List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptData.toolFeedbacks();

        List<HITLToolArgInfo> result = new LinkedList<>();
        for (InterruptionMetadata.ToolFeedback toolFeedback : toolFeedbacks) {
            if (toolFeedback.getName().equals(toolName)){
                ObjectMapper objectMapper = new ObjectMapper();

                String arguments = toolFeedback.getArguments();

                Map map = objectMapper.readValue(arguments, Map.class);
                map.forEach((k,v) -> {
                    result.add(new HITLToolArgInfo(k.toString(),v.toString()));
                });

            }
        }
        return result;
    }

    public List<CommonEnumVo> getStatusList() {
        List<CommonEnumVo> result = new LinkedList<>();
        for (HITLStatusEnum value : HITLStatusEnum.values()) {
            CommonEnumVo vo = new CommonEnumVo();

            vo.setToShow(value.name());
            vo.setToTransfer(value.description);

            result.add(vo);
        }

        return result;
    }
}
