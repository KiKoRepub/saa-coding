package org.cookpro.tools.alibaba;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.dto.agent.AgentStoreRequest;
import org.cookpro.dto.agent.AgentUserInfoDTO;
import org.cookpro.dto.agent.StoreUserRequest;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;
@Slf4j
public class UserSaveInfoTool implements BaseAlibabaToolInter<StoreUserRequest, Map<String,Object>>{


    @Override
    public Map<String,Object> apply(StoreUserRequest request, ToolContext toolContext) {

        RunnableConfig runnableConfig = (RunnableConfig) toolContext.getContext().get(AGENT_CONFIG);
        Store store = runnableConfig.store();
        Map<String, Object> value = request.getDto().toMap();
        StoreItem item = StoreItem.of(request.getNamespace(), request.getKey(), value);
        store.putItem(item);
        log.info("Saved user info to store, namespace: {}, key: {}", request.getNamespace(), request.getKey());
        return value;
    }
    @Override
    public String getToolName() {
        return getClass().getName();
    }

    @Override
    public String getToolDescription() {
        return "Save the current user's account information";
    }

    @Override
    public Class<StoreUserRequest> getInputType() {
       return StoreUserRequest.class;
    }

}
