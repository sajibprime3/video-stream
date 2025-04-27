package com.dark.videostreaming.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    
    public static final String VIDEO_BUCKET_NAME = "videos";
    public static final String PREVIEW_BUCKET_NAME = "previews";
    
    
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
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(VIDEO_BUCKET_NAME).build())) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(VIDEO_BUCKET_NAME)
                            .build()
            );
        }
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(PREVIEW_BUCKET_NAME).build())) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(PREVIEW_BUCKET_NAME)
                            .build()
            );
        }
        return client;
    }
    
    
}
