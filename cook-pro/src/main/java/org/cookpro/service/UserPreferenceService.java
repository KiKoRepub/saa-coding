package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.xdevapi.SchemaImpl;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.AgentBackground;
import org.cookpro.config.factory.AgentModelFactory;
import org.cookpro.dto.UserPreferenceDTO;
import org.cookpro.entity.ChatRecord;
import org.cookpro.entity.UserPreference;
import org.cookpro.mapper.UserPreferenceMapper;
import org.cookpro.utils.SystemPrinter;
import org.cookpro.vo.UserPreferenceVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Optional;

// TODO 拓展多条用户偏好分析记录，目前先以一条为限，后续可以增加多条分析记录，并提供查询接口
@Slf4j
@Service
public class UserPreferenceService extends ServiceImpl<UserPreferenceMapper, UserPreference> {

    @Resource
    private ChatRecordService chatRecordService;
    @Resource
    private RedisService redisService;
    @Resource
    private UserPreferenceMapper userPreferenceMapper;
    private final ChatClient chatClient;
    private static final AgentBackground BACKGROUND = AgentBackground.USER_PREFERENCE_ANALYZER;

    private static final String USER_PREFERENCE_FLAG_KEY_PREFIX = "user:preference:flag:";
    private static final Long USER_PREFERENCE_FLAG_TTL = 24 * 3L; // 24 * 3 小时 (3天)

    public UserPreferenceService(AgentModelFactory modelFactory) {
        Optional<ChatModel> chatModel = modelFactory.getAgentModel(BACKGROUND);
        ChatModel model;
        model = chatModel.orElse(null);
        this.chatClient = ChatClient.builder(model)
                .build();
    }

    public UserPreferenceVo getPreference(Long userId) {
        String cacheKey = getCacheKey(userId);

        UserPreferenceDTO dto = redisService.getWithCacheLoader(cacheKey, UserPreferenceDTO.class,
                () -> userPreferenceMapper.getOneByUserId(userId),
                USER_PREFERENCE_FLAG_TTL, TimeUnit.HOURS
        );

        return toVo(dto);

    }


    public boolean analyzePreferencesWithCache(Long userId) {
        if (redisService.exists(getCacheKey(userId))) {
            return true;
        } else return analyzePreferences(userId);
    }

    /**
     * 分析用户偏好(目前以一条为限，后续可以增加多条分析记录)
     */
    public boolean analyzePreferences(Long userId) {

        List<ChatRecord> history = chatRecordService.listByUserId(userId);

        if (history.isEmpty()) {
            return false;
        }

        // 构建分析提示
        StringBuilder messages = new StringBuilder();
        for (ChatRecord h : history) {
            messages.append("用户: ").append(h.getUserMessage()).append("\n");
            messages.append("助手: ").append(h.getBotResponse()).append("\n");
        }

        String analysisPrompt = "请分析以下用户的聊天记录，提取用户的兴趣、喜好和偏好\n\n" + messages;

        Prompt prompt = new Prompt(
                new SystemMessage(BACKGROUND.systemPrompt),
                new UserMessage(analysisPrompt));


        try {
            var responseDTO = chatClient
                    .prompt(prompt)
                    .call()
                    .entity(UserPreferenceDTO.class);
            SystemPrinter.println(responseDTO.getPreferContent());

            // 将偏好记录 保存到数据库 并缓存到Redis
            UserPreference preference = new UserPreference();
            preference.setUserId(userId);
            preference.setPreferContent(responseDTO.getPreferContent());


            save(preference);

            redisService.cacheObject(getCacheKey(userId), responseDTO, USER_PREFERENCE_FLAG_TTL, TimeUnit.HOURS);

            return true;
        } catch (Exception e) {
            log.error("分析用户偏好失败", e);
            return false;
        }
    }



    private String getCacheKey(Long userId) {
        return USER_PREFERENCE_FLAG_KEY_PREFIX + userId;
    }

    private UserPreferenceVo toVo(UserPreferenceDTO dto) {
        UserPreferenceVo vo = new UserPreferenceVo();
        BeanUtil.copyProperties(dto, vo);

        return vo;

    }
}
