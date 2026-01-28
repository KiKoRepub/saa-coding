package org.cookpro.service;


import org.cookpro.enums.MinioBucketEnum;
import org.cookpro.vo.ResourceUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUploadService {

    private final MinIoService minIoService;

    private static final Integer DEFAULT_EXPIRE_TIME = 0;

    private static final String DEFAULT_BUCKET_NAME = MinioBucketEnum.PUBLIC.value;

    public FileUploadService(MinIoService minIoService) {
        this.minIoService = minIoService;
    }

    public ResourceUploadVo uploadFile(MultipartFile file) throws IOException {

        int ran2 = (int) (Math.random() * (100 - 1) + 1);

        String fileName = String.valueOf(System.currentTimeMillis()) + ran2;


        final String originalFilename = file.getOriginalFilename();


        String toUploadName = fileName +
                originalFilename.substring(originalFilename.lastIndexOf("."));

        String path = "" + toUploadName;

        minIoService.putObject(DEFAULT_BUCKET_NAME, toUploadName,
                file.getInputStream(), file.getContentType());
        return new ResourceUploadVo(path,originalFilename,toUploadName);

    }
    public List<ResourceUploadVo> batchUploadFile(MultipartFile[] files) throws IOException {

        List<ResourceUploadVo> uploadVos = new ArrayList<>();
        for (MultipartFile file : files) {
            // 不需要 判断 content , 文本类型一定是 上传的文本文件
            uploadVos.add(uploadFile(file));
        }
        // 打印上传结果
        uploadVos.forEach(vo -> System.out.println("上传结果: " + vo.getPath()));

        return uploadVos;
    }
    public void removeObject(String objectName) {
        minIoService.removeObject(DEFAULT_BUCKET_NAME, objectName);
    }

    public boolean objectExists(String objectName) {
        try {
            return minIoService.getObject(DEFAULT_BUCKET_NAME, objectName) != null;
        }catch (Exception e){
            return false;
        }
    }


    public String getPreviewUrl(String objectName) {
        if (objectExists(objectName))
            return minIoService.getObjectUrl(DEFAULT_BUCKET_NAME, objectName,DEFAULT_EXPIRE_TIME);
        else return objectName;
    }

//    public String getPreviewUrl(String objectName, Integer expireSeconds) {
//
//        minIoService.getObjectUrl()
//        if (objectExists(objectName))
//            return minIoService.getObjectUrl(DEFAULT_BUCKET_NAME, objectName, expireSeconds);
//        else return objectName;
//    }

    public static Integer getExpireSeconds(){
        return DEFAULT_EXPIRE_TIME == 0 ? 604800 : DEFAULT_EXPIRE_TIME;
    }
}
