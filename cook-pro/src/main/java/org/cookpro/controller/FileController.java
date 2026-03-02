package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.service.FileUploadService;
import org.cookpro.vo.ResourceUploadVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {


    @Resource
    FileUploadService fileUploadService;



    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "上传文件", description = "上传一个文件并返回其访问路径")
    public R<ResourceUploadVo> uploadFile(@RequestParam("file")MultipartFile file) throws Exception {
        return R.ok(fileUploadService.uploadFile(file));
    }

    @PostMapping(value = "/batchUpload", consumes = "multipart/form-data")
    @Operation(summary = "批量上传文件", description = "上传多个文件并返回它们的访问路径")
    public R<List<ResourceUploadVo>> batchUploadFile(@RequestParam("files") MultipartFile[] files) throws Exception {
        return R.ok(fileUploadService.batchUploadFile(files));
    }

    @PostMapping("/remove")
    @Operation(summary = "删除文件", description = "根据文件名删除文件")
    public R<String> removeFile(@RequestParam("objectName") String objectName) {
        fileUploadService.removeObject(objectName);
        return R.ok("文件删除成功");
    }

    @PostMapping("/preview")
    @Operation(summary = "预览文件", description = "根据文件名检查文件是否存在")
    public R<String> previewFile(@RequestParam("objectName") String objectName) {
        return R.ok(fileUploadService.getPreviewUrl(objectName));
    }

}
