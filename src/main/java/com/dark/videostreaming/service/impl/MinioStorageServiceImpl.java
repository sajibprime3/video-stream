package com.dark.videostreaming.service.impl;

import com.dark.videostreaming.config.MinioConfig;
import com.dark.videostreaming.service.MinioStorageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MinioStorageServiceImpl implements MinioStorageService {
    
    private final MinioClient client;
    
    @Value("${minio.object-part-size}")
    private Long objectPartSize;
    
    @Override
    public void save(MultipartFile file, UUID uuid) throws Exception {
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(MinioConfig.BUCKET_NAME)
                        .object(uuid.toString())
                        .stream(file.getInputStream(), file.getSize(), objectPartSize)
                        .build()
        );
    }
    
    @Override
    public InputStream getInputStream(UUID uuid, long offset, long length) throws Exception {
        return client.getObject(
                GetObjectArgs.builder()
                        .bucket(MinioConfig.BUCKET_NAME)
                        .object(uuid.toString())
                        .offset(offset)
                        .length(length)
                        .build()
        );
    }
}
