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
import org.cookpro.entity.HITLToolArgInfo;
import org.cookpro.entity.HITLEntity;
import org.cookpro.enums.HITLStatusEnum;
import org.cookpro.enums.SSEEventEnum;
import org.cookpro.mapper.HITLMapper;
import org.cookpro.utils.HITLHelper;
import org.cookpro.utils.SystemPrinter;
import org.cookpro.vo.CommonEnumVo;
import org.cookpro.vo.HITLPageVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HITLService extends ServiceImpl<HITLMapper, HITLEntity> {

    @Resource
    ReactAgent dashscopeHITLAgent;

    @Resource
    ThreadPoolExecutor asyncExecutor;

    @Resource
    SSEService sseService;

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

    public String reviewHitl(HITLReviewDTO dto) throws GraphRunnerException, IOException, InterruptedException {
        Long id = dto.getId();


        HITLEntity hitlEntity = getById(id);


        String threadId = hitlEntity.getThreadId();
        InterruptionMetadata interruptionMetadata = hitlEntity.getInterruptData();
        Long reviewerId = hitlEntity.getReviewerId();
        Long publisherId = hitlEntity.getPublisherId();

        String message = dto.getMessage();
        boolean approved = dto.isApproved();

        // 获取 审核结果
        InterruptionMetadata approvalMetadata = getReviewResult(interruptionMetadata,approved);
        if (approved){
            sseService.sendMessage(reviewerId,publisherId,
                    SSEEventEnum.REVIEW_PASSED.eventName,
                    "您的人工介入请求已通过审核,正在恢复执行..."
            );
        }else {
            sseService.sendMessage(reviewerId,publisherId,
                    SSEEventEnum.REVIEW_REJECTED.eventName,
                    "您的人工介入请求未通过审核,原因是:"+ dto.getReviewComment()
            );
        }

        // 创建恢复配置
        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                .build();
        // 创建异步任务 恢复 执行
        createAsyncTask(message, resumeConfig,
                hitlEntity.getReviewerId(), hitlEntity.getPublisherId());


        return hitlEntity.getId().toString();
    }

    public String editHitl(HITLEditInfoDTO dto) throws GraphRunnerException {

        Long id = dto.getId();
        HITLEntity hitlEntity = getById(id);

        String threadId = hitlEntity.getThreadId();
        InterruptionMetadata interruptionMetadata = hitlEntity.getInterruptData();

        String message = dto.getMessage();

        List<HITLToolArgInfo> infoList = dto.getArgInfoList();
        String toolName = dto.getToolName();

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
        createAsyncTask(message,
                resumeConfig,
                hitlEntity.getReviewerId(),
                hitlEntity.getPublisherId()
        );



        return hitlEntity.getId().toString();
    }
    @NotNull
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


        return queryWrapper;

    }


    @NotNull
    private HITLPageVo toPageVo(HITLEntity entity) {
        HITLPageVo hitlPageVo = new HITLPageVo();

        BeanUtil.copyProperties(entity, hitlPageVo);

        return hitlPageVo;
    }

    private void createAsyncTask(String message, RunnableConfig resumeConfig,Long userId,Long toId) throws GraphRunnerException {
        CompletableFuture.runAsync(() -> {
            try {
                Optional<NodeOutput> finalResult = dashscopeHITLAgent.invokeAndGetOutput(message, resumeConfig);

                if (finalResult.isPresent()) {

                    AssistantMessage response = HITLHelper.getAssistantResponse(finalResult.get().state());

                    System.out.println(response.getText());

                    sseService.sendMessage(userId,toId,
                            SSEEventEnum.COMPLETED.eventName,
                            "执行已完成,结果为: " + response.getText());


                }
            } catch (GraphRunnerException e) {
                log.error("异步恢复执行失败", e);
                throw new RuntimeException(e);
            } catch (IOException | InterruptedException e) {
                log.error("sse 推送失败", e);
                throw new RuntimeException(e);
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
