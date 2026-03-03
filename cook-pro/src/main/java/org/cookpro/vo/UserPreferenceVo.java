package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserPreferenceVo {

//    String cuisinePreference; // 喜好菜系
    @Schema(description = "喜好内容")
   private String preferContent; // 喜好内容
}
