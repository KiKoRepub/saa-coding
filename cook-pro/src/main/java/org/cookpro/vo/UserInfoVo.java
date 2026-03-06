package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserInfoVo {

    @Schema(description = "用户昵称")
    private String userName;
    @Schema(description = "用户头像路径")
    private String avatarUrl;
    @Schema(description = "用户角色")
    private String role;
    @Schema(description = "用户偏好信息")
    private Object preference;

}
