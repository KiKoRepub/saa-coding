package org.cookpro.tools.alibaba;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.cookpro.dto.agent.AgentStoreRequest;
import org.cookpro.dto.agent.AgentUserInfoDTO;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;
import java.util.Optional;

public class UserGetInfoTool implements BaseAlibabaToolInter<AgentStoreRequest, AgentUserInfoDTO> {

    @Override
    public AgentUserInfoDTO apply(AgentStoreRequest request, ToolContext context) {

        RunnableConfig runnableConfig = (RunnableConfig) context.getContext().get("_AGENT_CONFIG_");
        Store store = runnableConfig.store();
        Optional<StoreItem> itemOpt = store.getItem(request.namespace(), request.key());
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
    public Class<AgentStoreRequest> getInputType() {
        return AgentStoreRequest.class;
    }
}
