package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserPreferenceDTO {

    @Schema(description = "喜好内容")
    private String preferContent;

}
