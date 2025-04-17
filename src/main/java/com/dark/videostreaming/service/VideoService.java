package com.dark.videostreaming.service;

import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.util.Range;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface VideoService {
    
    FileDto save(MultipartFile video, String title);
    
    ChunkWithMetadata fetchChunk(UUID uuid, Range range);
    
    List<FileDto> getAllInfo();
    
    FileDto getInfoById(long id);
    
    
}
