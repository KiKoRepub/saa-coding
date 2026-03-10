package org.cookpro.tools.alibaba;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.cookpro.dto.agent.AgentStoreRequest;
import org.cookpro.dto.agent.AgentUserInfoDTO;
import org.cookpro.dto.agent.StoreUserRequest;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;
import java.util.Optional;

public class UserGetInfoTool implements BaseAlibabaToolInter<StoreUserRequest, AgentUserInfoDTO> {

    @Override
    public AgentUserInfoDTO apply(StoreUserRequest request, ToolContext context) {

        RunnableConfig runnableConfig = (RunnableConfig) context.getContext().get("");
        Store store = runnableConfig.store();
        Optional<StoreItem> itemOpt = store.getItem(request.getNamespace(), request.getKey());
        if (itemOpt.isPresent()) {
            Map<String, Object> value = itemOpt.get().getValue();


            return new AgentUserInfoDTO(value.get("userName").toString());
        }
        return new AgentUserInfoDTO(null);
    }


    @Override
    public String getToolName() {
        return UserGetInfoTool.class.getName();
    }

    @Override
    public String getToolDescription() {
        return "Get the current user's account information";
    }

    @Override
    public Class<StoreUserRequest> getInputType() {
        return StoreUserRequest.class;
    }
}
