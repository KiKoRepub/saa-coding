package org.cookpro.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResourceUploadVo {
    @Schema(description = "资源路径,文本资源的时候 直接存储 文本内容", example = "")
    private String path;
    @Schema(description = "原始文件名称", example = "")
    private String fileName;
    @Schema(description = "对象名称(minio)", example = "")
    private String objectName;
    public ResourceUploadVo(String path, String fileName, String objectName) {
        this.path = path;
        this.fileName = fileName;
        this.objectName = objectName;
    }
}
