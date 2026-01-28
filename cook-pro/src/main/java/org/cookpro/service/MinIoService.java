package org.cookpro.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MinIoService {


    private final MinioClient minioClient;

    public MinIoService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    public InputStream getObject(String bucketName, String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            return minioClient.getObject(args);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName
     * @param objectName
     * @param contentType
     */
    public void putObject(String bucketName, String objectName, String contentType) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName
     * @param objectName
     * @param content
     */
    public void putObjectForContent(String bucketName, String objectName, String content) {
        try {
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            putObject(bucketName, objectName, inputStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName
     * @param objectName
     * @param stream
     */
    public void putObject(String bucketName, String objectName, InputStream stream) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName
     * @param objectName
     * @param stream
     * @param contentType
     */
    public void putObject(String bucketName, String objectName, InputStream stream, String contentType) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(contentType)
                    .stream(stream, stream.available(), -1)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName
     * @param objectName
     */
    public void removeObject(String bucketName, String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            minioClient.removeObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * 获取文件url
     *
     * @param bucketName
     * @param objectName
     */
    public String getObjectUrl(String bucketName, String objectName, Integer expires) {
        try {
            if (expires == 0) {
                //minio最大值 0会报错
                expires = 604800;
            }
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expires)
                    .method(Method.GET)
                    .build();
            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }


}
