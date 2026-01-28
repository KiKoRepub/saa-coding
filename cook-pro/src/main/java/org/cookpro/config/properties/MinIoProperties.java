package org.cookpro.config.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "minio")
public class MinIoProperties {

    private String endPoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String pathPrefix;

    public String getPathPrefix() {
        return pathPrefix;
    }

    public MinIoProperties setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
        return this;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public MinIoProperties setEndPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public MinIoProperties setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public MinIoProperties setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public MinIoProperties setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }
}
