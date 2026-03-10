package org.cookpro.dto.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
public class AgentUserInfoDTO {

// 用户信息：姓名=%s, 年龄=%s, 邮箱=%s, 偏好=%s
    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "偏好")
    private String preference;

    public AgentUserInfoDTO(String userName) {
        this.userName = userName;
    }



    public Map<String,Object> toMap(){
        // 动态构造
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(this, Map.class);
        }catch (Exception e){
            e.printStackTrace();
            return Map.of();
        }
    }
}
