package org.cookpro.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/*
Agent Store 请求对象(长期记忆)
 */
public record AgentStoreRequest(List<String> namespace, String key) {}
