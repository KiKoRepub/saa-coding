package org.cookpro.service;

import lombok.extern.slf4j.Slf4j;
import org.cookpro.AgentBackground;
import org.cookpro.config.factory.AgentModelFactory;
import org.cookpro.entity.ChatRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserPreferenceService {

    @Resource
    private ChatRecordService chatRecordService;

    private final ChatModel model;
    private static final AgentBackground BACKGROUND = AgentBackground.USER_PREFERENCE_ANALYZER;

    public UserPreferenceService(AgentModelFactory modelFactory) {
        Optional<ChatModel> chatModel = modelFactory.getAgentModel(BACKGROUND);

        if (chatModel.isPresent()) {
            this.model = chatModel.get();
        } else {
            model = null;
        }
    }

    /**
     * 分析用户偏好
     */
    public String analyzeUserPreferences(Long userId) {



        List<ChatRecord> history = chatRecordService.listByUserId(userId);

        if (history.isEmpty()) {
            return "无足够数据分析用户偏好。";
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
            var response = model.call(prompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("分析用户偏好失败", e);
            return "分析失败: " + e.getMessage();
        }
    }

}
