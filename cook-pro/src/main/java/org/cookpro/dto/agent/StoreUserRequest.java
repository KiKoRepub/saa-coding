package org.cookpro.dto.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreUserRequest extends AgentStoreRequest{

    AgentUserInfoDTO dto;

}
