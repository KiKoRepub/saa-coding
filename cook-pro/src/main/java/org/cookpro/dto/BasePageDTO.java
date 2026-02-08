package org.cookpro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.bouncycastle.pqc.crypto.newhope.NHSecretKeyProcessor;

@Data
public class BasePageDTO {

    @Schema(description = "页码，默认为1")
    private int pageNum = 1;
    @Schema(description = "每页条数，默认为10")
    private int pageSize = 10;

}
