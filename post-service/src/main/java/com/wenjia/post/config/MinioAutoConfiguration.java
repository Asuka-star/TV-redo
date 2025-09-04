package com.wenjia.post.config;


import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio自动配置.
 *
 * @author Mei rx
 * @since 2021/08/05
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@ConditionalOnClass({MinioClient.class})
@Data
public class MinioAutoConfiguration {

    String endpoint ;
    String accessKey ;
    String secretKey ;
    String bucketName;

    @Bean
    public MinioClient minioClient() throws MinioException {
        MinioClient minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        this.makeBucket(minioClient, bucketName);
        return minioClient;
    }
 
    private void makeBucket(MinioClient minioClient, String bucketName) throws MinioException{
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new MinioException("创建minio存储桶异常", e.getMessage());
        }
    }
}