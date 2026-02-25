package org.cookpro.config.factory;

import jakarta.annotation.Resource;
import org.cookpro.AgentBackground;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AgentModelFactory {

    @Resource
    private ApplicationContext context;
    public Optional<ChatModel> getAgentModel(AgentBackground background){
        try {
            Class<? extends ChatModel> modelClass = background.clazz;

            return Optional.of(context.getBean(modelClass));
        } catch (Exception e){
            return Optional.empty();
        }
    }
}
