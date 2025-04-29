package com.dark.videostreaming.service.impl;

import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.entity.File;
import com.dark.videostreaming.entity.FileMetadata;
import com.dark.videostreaming.entity.Preview;
import com.dark.videostreaming.mapper.FileMapper;
import com.dark.videostreaming.repository.FileMetadataRepository;
import com.dark.videostreaming.repository.FileRepository;
import com.dark.videostreaming.repository.PreviewRepository;
import com.dark.videostreaming.service.PreviewGeneratorService;
import com.dark.videostreaming.service.PreviewStorageService;
import com.dark.videostreaming.service.VideoService;
import com.dark.videostreaming.service.VideoStorageService;
import com.dark.videostreaming.util.ChunkReader;
import com.dark.videostreaming.util.Range;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@EnableAsync
@Service
public class VideoServiceImpl implements VideoService {
    
    private final VideoStorageService videoStorageService;
    
    private final PreviewStorageService previewStorageService;
    
    private final PreviewGeneratorService previewGeneratorService;
    
    private final FileMetadataRepository metadataRepository;
    
    private final FileRepository fileRepository;
    
    private final PreviewRepository previewRepository;
    
    private final FileMapper fileMapper;
    
    
    @Override
    @Transactional
    public FileDto save(MultipartFile video, String title) {
        //Todo Validate The video. i.e size.
        try {
            FileMetadata metadata = FileMetadata.builder()
                    .size(video.getSize())
                    .HttpContentType(video.getContentType())
                    .build();
            File file = File.builder()
                    .title(title)
                    .publishDate(LocalDate.now())
                    .metadata(metadata)
                    .build();
            
            UUID fileUuid = metadataRepository.save(metadata).getUuid();
            
            videoStorageService.save(video.getInputStream(), fileUuid.toString(), video.getSize());
            Instant instant = Instant.now();
            Preview preview = Preview.builder()
                    .file(file)
                    .name(fileUuid + "_preview_" + instant)
                    .createdAt(instant)
                    .status(Preview.PreviewStatus.PENDING)
                    .build();
            Preview savedPreview = previewRepository.save(preview);
            file.setPreview(savedPreview);
            File savedFile = fileRepository.save(file);
            previewGeneratorService.generatePreview(savedFile.getId());
            return fileMapper.fileToFileDto(savedFile);
        } catch (Exception ex) {
            log.error("Exception occurred when trying to save the file:", ex);
            throw new RuntimeException(ex);
        }
        
    }
    
    @Override
    public ChunkWithMetadata fetchVideoChunk(UUID uuid, Range range) {
        FileMetadata metadata = metadataRepository.findById(uuid).orElseThrow();
        return new ChunkWithMetadata(metadata.getUuid().toString(),
                metadata.getSize(),
                metadata.getHttpContentType(),
                ChunkReader.read(metadata.getUuid().toString(), range, metadata.getSize(), videoStorageService)
        );
    }

    @Override
    public ChunkWithMetadata fetchPreviewChunk(long id, Range range) {
        Preview preview = previewRepository.findById(id).orElseThrow();
        
        return new ChunkWithMetadata(
                preview.getName(),
                preview.getSize(),
                "application/octet-stream",
                ChunkReader.read(preview.getName(), range, preview.getSize(), previewStorageService)
        );
    }


    @Override
    public List<FileDto> getAllInfo() {
        return fileRepository.findAll().stream().map(fileMapper::fileToFileDto).toList();
    }

    @Override
    public FileDto getInfoById(long id) {
        return fileMapper.fileToFileDto(fileRepository.findById(id).orElseThrow());
    }

    @Override
    public void requestPreviewGeneration(long id) {
        //Todo Validate request. i.e check if Video exists, size etc.
        previewGeneratorService.generatePreview(id);
    }
    
}
