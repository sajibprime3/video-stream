package com.dark.videostreaming.service;

import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.util.Range;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface VideoService {
    
    UUID save(MultipartFile video);
    
    ChunkWithMetadata fetchChunk(UUID uuid, Range range);
    
}
