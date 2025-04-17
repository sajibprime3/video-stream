package com.dark.videostreaming.service.impl;

import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.entity.File;
import com.dark.videostreaming.entity.FileMetadata;
import com.dark.videostreaming.mapper.FileMapper;
import com.dark.videostreaming.repository.FileMetadataRepository;
import com.dark.videostreaming.repository.FileRepository;
import com.dark.videostreaming.service.MinioStorageService;
import com.dark.videostreaming.service.VideoService;
import com.dark.videostreaming.util.Range;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    
    private final MinioStorageService storageService;
    
    private final FileMetadataRepository metadataRepository;
    
    private final FileRepository fileRepository;
    
    private final FileMapper fileMapper;
    
    
    @Override
    @Transactional
    public FileDto save(MultipartFile video, String title) {
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
            
            storageService.save(video, fileUuid);
            
            return fileMapper.fileToFileDto(fileRepository.save(file));
        } catch (Exception ex) {
            log.error("Exception occurred when trying to save the file:", ex);
            throw new RuntimeException(ex);
        }
        
    }

    @Override
    public ChunkWithMetadata fetchChunk(UUID uuid, Range range) {
        FileMetadata metadata = metadataRepository.findById(uuid).orElseThrow();
        return new ChunkWithMetadata(metadata, readChunk(uuid, range, metadata.getSize()));
    }

    @Override
    public List<FileDto> getAllInfo() {
        return fileRepository.findAll().stream().map(fileMapper::fileToFileDto).toList();
    }

    @Override
    public FileDto getInfoById(long id) {
        return fileMapper.fileToFileDto(fileRepository.findById(id).orElseThrow());
    }


    public byte[] readChunk(UUID uuid, Range range, long fileSize) {
        long startPosition = range.getRangeStart();
        long endPosition = range.getRangeEnd(fileSize);
        int chunkSize = (int) (endPosition - startPosition + 1);
        try(InputStream inputStream = storageService.getInputStream(uuid, startPosition, chunkSize)) {
            return inputStream.readAllBytes();
        } catch (Exception ex) {
            log.error("Exception occurred when trying to read file with ID = {}", uuid);
            throw new RuntimeException(ex);
        }
    }
}
