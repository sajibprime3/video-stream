package com.dark.videostreaming.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    
    public static final String BUCKET_NAME = "videos";
    
    
    @Value("${minio.url}")
    private String minioUrl;
    
    @Value("${minio.username}")
    private String minioUser;
    
    @Value("${minio.password}")
    private String minioPassword;
    
    @Bean
    public MinioClient minioClient() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioUser, minioPassword)
                .build();
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(BUCKET_NAME)
                            .build()
            );
        }
        
        return client;
    }
    
    
}
