package com.dark.videostreaming.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface MinioStorageService {
    
    void save(MultipartFile file, UUID uuid) throws Exception;
    
    InputStream getInputStream(UUID uuid, long offset, long length) throws Exception;
    
}
