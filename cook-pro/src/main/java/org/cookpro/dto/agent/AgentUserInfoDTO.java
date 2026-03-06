package org.cookpro.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AgentUserInfoDTO {


    @Schema(description = "用户名")
    private String userName;

    public AgentUserInfoDTO(String userName) {
        this.userName = userName;
    }
}
