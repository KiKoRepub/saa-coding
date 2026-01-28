package org.cookpro.config;

import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.config.properties.MinIoProperties;
import org.cookpro.enums.MinioBucketEnum;
import org.cookpro.service.FileUploadService;
import org.cookpro.service.MinIoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@Configuration
@EnableConfigurationProperties({MinIoProperties.class})
public class MinIoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinIoConfig.class);

    private MinioClient minioClient;

    @Autowired
    private MinIoProperties minIoProperties;


    @Bean
    @ConditionalOnProperty(name = "minio.endPoint")
    public MinIoService minIoService() {
        try {
            if (StringUtils.isEmpty(minIoProperties.getEndPoint()) ||
                    StringUtils.isEmpty(minIoProperties.getAccessKey()) ||
                    StringUtils.isEmpty(minIoProperties.getSecretKey())) {
                throw new Exception("[CONFIG] minon properties is not found");
            }
            minioClient = MinioClient.builder()
                    .endpoint(minIoProperties.getEndPoint())
                    .credentials(minIoProperties.getAccessKey(), minIoProperties.getSecretKey())
                    .build();

            String bucketName = minIoProperties.getBucketName();
            String policyJson = """
                    {
                         "Version": "2012-10-17",
                         "Statement": [
                             {
                                 "Effect": "Allow",
                                 "Action": [
                                     "s3:*"
                                 ],
                                 "Resource": [
                                     "arn:aws:s3:::*"
                                 ]
                             }
                         ]
                     }
                    """.formatted(bucketName,bucketName);

            SetBucketPolicyArgs args = SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policyJson)
                    .build();

            minioClient.setBucketPolicy(args);

        } catch (Exception e) {
            logger.error("[CONFIG] minio config is error");
            log.error(e.getMessage(), e);
        }
        logger.info("Minio Initializing server '{}' ", minIoProperties.getEndPoint());

        return new MinIoService(minioClient);
    }

    @Bean
    @ConditionalOnProperty(name = "minio.endPoint")
    public FileUploadService upLoadService(MinIoService minIoService) {
        return new FileUploadService(minIoService);
    }
}
