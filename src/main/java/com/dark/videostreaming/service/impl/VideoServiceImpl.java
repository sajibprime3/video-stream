package com.dark.videostreaming.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.entity.File;
import com.dark.videostreaming.entity.FileMetadata;
import com.dark.videostreaming.entity.Preview;
import com.dark.videostreaming.entity.Thumbnail;
import com.dark.videostreaming.event.PreviewCreationEvent;
import com.dark.videostreaming.mapper.FileMapper;
import com.dark.videostreaming.repository.FileMetadataRepository;
import com.dark.videostreaming.repository.FileRepository;
import com.dark.videostreaming.repository.PreviewRepository;
import com.dark.videostreaming.repository.ThumbnailRepository;
import com.dark.videostreaming.service.PreviewStorageService;
import com.dark.videostreaming.service.ThumbnailStorageService;
import com.dark.videostreaming.service.VideoService;
import com.dark.videostreaming.service.VideoStorageService;
import com.dark.videostreaming.util.ChunkReader;
import com.dark.videostreaming.util.Range;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@EnableAsync
@Service
public class VideoServiceImpl implements VideoService {

    private final VideoStorageService videoStorageService;

    private final PreviewStorageService previewStorageService;

    private final ThumbnailStorageService thumbnailStorageService;

    private final FileMetadataRepository metadataRepository;

    private final FileRepository fileRepository;

    private final PreviewRepository previewRepository;

    private final ThumbnailRepository thumbnailRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final FileMapper fileMapper;

    @Override
    @Transactional
    public FileDto save(MultipartFile video, String title) {
        // Todo Validate The video. i.e size.
        if (video.getContentType() == null) {
            // add custom errors to handle problems with the uploaded video.
            throw new Error();
        }
        try {
            FileMetadata metadata = FileMetadata.builder()
                    .size(video.getSize())
                    .mediaType(MediaType.parseMediaType(video.getContentType()))
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
            Thumbnail thumbnail = Thumbnail.builder()
                    .file(file)
                    .name(fileUuid + "_thumbnail_" + instant)
                    .createdAt(instant)
                    .status(Thumbnail.ThumbnailStatus.PENDING)
                    .build();
            Thumbnail savedThumbnail = thumbnailRepository.save(thumbnail);
            File savedFile = fileRepository.save(file);
            // FIX: Use Kafka instead.
            eventPublisher.publishEvent(new PreviewCreationEvent(savedFile.getId()));
            return fileMapper.fileToFileDto(savedFile);
        } catch (Exception ex) {
            log.error("Exception occurred when trying to save the file:", ex);
            throw new RuntimeException(ex);
        }

    }

    @Transactional
    @Override
    public void deleteVideo(long id) {
        if (!fileRepository.existsById(id))
            throw new RuntimeException("Video not found!");
        File fileToDelete = fileRepository.findById(id).orElseThrow();
        if (!metadataRepository.existsById(fileToDelete.getMetadata().getUuid()))
            throw new RuntimeException("FileMetadata not found!");
        if (!previewRepository.existsById(fileToDelete.getPreview().getId()))
            throw new RuntimeException("Preview not found!");
        try {
            videoStorageService.delete(fileToDelete.getMetadata().getUuid().toString());
            previewStorageService.delete(fileToDelete.getPreview().getName());
        } catch (Exception e) {
            throw new RuntimeException("Couldn't delete Objects from storage", e);
        }
        metadataRepository.deleteById(fileToDelete.getMetadata().getUuid());
        previewRepository.deleteById(fileToDelete.getPreview().getId());
        fileRepository.deleteById(fileToDelete.getId());
    }

    @Override
    public ChunkWithMetadata fetchVideoChunk(UUID uuid, Range range) {
        FileMetadata metadata = metadataRepository.findById(uuid).orElseThrow();
        return new ChunkWithMetadata(metadata.getUuid().toString(),
                metadata.getSize(),
                metadata.getMediaType(),
                ChunkReader.read(metadata.getUuid().toString(), range, metadata.getSize(), videoStorageService));
    }

    @Override
    public ChunkWithMetadata fetchPreviewChunk(long id, Range range) {
        Preview preview = previewRepository.findById(id).orElseThrow();

        return new ChunkWithMetadata(
                preview.getName(),
                preview.getSize(),
                MediaType.APPLICATION_OCTET_STREAM,
                ChunkReader.read(preview.getName(), range, preview.getSize(), previewStorageService));
    }

    @Override
    public ChunkWithMetadata fetchThumbnail(long id) {
        // Todo add checks for thumbnails. like is there a any valid thumbnails
        // available.
        Thumbnail thumbnail = fileRepository.findById(id).orElseThrow().getThumbnail();
        return new ChunkWithMetadata(
                thumbnail.getName(),
                thumbnail.getSize(),
                MediaType.IMAGE_JPEG,
                ChunkReader.read(thumbnail.getName(), thumbnail.getSize(), thumbnailStorageService));
    }

    @Override
    public List<FileDto> getAllInfo() {
        return fileRepository.findAll().stream().map(fileMapper::fileToFileDto).toList();
    }

    @Override
    public FileDto getInfoById(long id) {
        return fileMapper.fileToFileDto(fileRepository.findById(id).orElseThrow());
    }

    @Transactional
    @Override
    public void requestPreviewGeneration(long id) {
        // Todo Validate request. i.e check if Video exists, size etc.
        // FIX: (refactor) Use Kafak instead.
        eventPublisher.publishEvent(new PreviewCreationEvent(id));
    }

}
