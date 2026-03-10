package org.cookpro.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/*
Agent Store 请求对象(长期记忆)
 */
@Data
public class AgentStoreRequest{

    private  List<String> namespace;
    private  String key;
}
