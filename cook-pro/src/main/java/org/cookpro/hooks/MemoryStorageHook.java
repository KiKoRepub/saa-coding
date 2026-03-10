package org.cookpro.hooks;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/*
 * 记忆存储 Hook：在模型调用前从记忆存储加载用户画像并注入到消息中
 */
@Slf4j
@Component
public class MemoryStorageHook extends ModelHook {  
    @Override
    public String getName() {
        return "memory_storage_hook";
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL};
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        // 从配置中获取用户ID
        String userId = (String) config.metadata("user_id").orElse(null);
        List<String> namespace = (List<String>) config.metadata("namespace").orElse(null);

        if (userId == null || CollectionUtil.isEmpty(namespace)) {
            return CompletableFuture.completedFuture(Map.of());
        }

        Store store = config.store();
        // 从记忆存储中加载用户画像
        Optional<StoreItem> itemOpt = store.getItem(namespace, userId);
        if (itemOpt.isPresent()) {
            Map<String, Object> profile = itemOpt.get().getValue();

            // 将用户上下文注入系统消息
            String userContext = String.format(
                    "用户信息：姓名=%s, 年龄=%s, 邮箱=%s, 偏好=%s",
                    profile.get("name"),
                    profile.get("age"),
                    profile.get("email"),
                    profile.get("preferences")
            );

            // 获取消息列表
            List<Message> messages = (List<Message>) state.value("messages").orElse(new ArrayList<>());
            List<Message> newMessages = new ArrayList<>();

            // 查找是否已存在 SystemMessage
            SystemMessage existingSystemMessage = null;
            int systemMessageIndex = -1;
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                if (msg instanceof SystemMessage) {
                    existingSystemMessage = (SystemMessage) msg;
                    systemMessageIndex = i;
                    break;
                }
            }

            // 如果找到 SystemMessage，更新它；否则创建新的
            SystemMessage enhancedSystemMessage;
            if (existingSystemMessage != null) {
                // 更新现有的 SystemMessage
                enhancedSystemMessage = new SystemMessage(
                        existingSystemMessage.getText() + """
                        
                        """+ userContext
                );
            } else {
                // 创建新的 SystemMessage
                enhancedSystemMessage = new SystemMessage(userContext);
            }

            // 构建新的消息列表
            if (systemMessageIndex >= 0) {
                // 如果找到了 SystemMessage，替换它
                for (int i = 0; i < messages.size(); i++) {
                    if (i == systemMessageIndex) {
                        newMessages.add(enhancedSystemMessage);
                    } else {
                        newMessages.add(messages.get(i));
                    }
                }
            } else {
                // 如果没有找到 SystemMessage，在开头添加新的
                newMessages.add(enhancedSystemMessage);
                newMessages.addAll(messages);
            }

            return CompletableFuture.completedFuture(Map.of("messages", newMessages));
        }

        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        // 可以在这里实现对话后的记忆保存逻辑
        log.info("Start saving user conversations to memory storage...");




        return CompletableFuture.completedFuture(Map.of());
    }
}
