package com.dark.videostreaming.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.transaction.Transactional;

import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.dto.MetadataDto;
import com.dark.videostreaming.entity.File;
import com.dark.videostreaming.entity.Metadata;
import com.dark.videostreaming.entity.Preview;
import com.dark.videostreaming.entity.Preview.PreviewStatus;
import com.dark.videostreaming.entity.Thumbnail;
import com.dark.videostreaming.entity.Thumbnail.ThumbnailStatus;
import com.dark.videostreaming.event.Event;
import com.dark.videostreaming.event.model.PreviewUpdateEvent;
import com.dark.videostreaming.event.model.ThumbnailUpdateEvent;
import com.dark.videostreaming.event.model.VideoUploadedEvent;
import com.dark.videostreaming.mapper.FileMapper;
import com.dark.videostreaming.mapper.MetadataMapper;
import com.dark.videostreaming.repository.FileRepository;
import com.dark.videostreaming.repository.MetadataRepository;
import com.dark.videostreaming.repository.PreviewRepository;
import com.dark.videostreaming.repository.ThumbnailRepository;
import com.dark.videostreaming.service.PreviewStorageService;
import com.dark.videostreaming.service.ThumbnailStorageService;
import com.dark.videostreaming.service.VideoService;
import com.dark.videostreaming.service.VideoStorageService;

import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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

    private final MetadataRepository metadataRepository;

    private final FileRepository fileRepository;

    private final PreviewRepository previewRepository;

    private final ThumbnailRepository thumbnailRepository;

    private final KafkaTemplate<String, Event<?>> kafkaTemplate;

    private final FileMapper fileMapper;

    private final MetadataMapper metadataMapper;

    @Override
    @Transactional
    public FileDto save(MultipartFile video, String title) {
        // Todo Validate The video. i.e size.
        if (video.getContentType() == null) {
            // add custom errors to handle problems with the uploaded video.
            throw new Error();
        }
        try {
            Metadata metadata = Metadata.builder()
                    .size(video.getSize())
                    .mediaType(MediaType.parseMediaType(video.getContentType()))
                    .build();
            File file = File.builder()
                    .title(title)
                    .publishDate(LocalDate.now())
                    .metadata(metadata)
                    .build();

            metadataRepository.save(metadata);
            File savedFile = fileRepository.save(file);
            metadata = savedFile.getMetadata();
            metadata.setName(savedFile.getUuid().toString());
            metadataRepository.save(metadata);
            videoStorageService.save(video.getInputStream(), metadata.getName(), metadata.getSize());
            Instant instant = Instant.now();
            Preview preview = Preview.builder()
                    .file(file)
                    .createdAt(instant)
                    .status(Preview.PreviewStatus.PENDING)
                    .build();
            Preview savedPreview = previewRepository.save(preview);
            file.setPreview(savedPreview);
            Thumbnail thumbnail = Thumbnail.builder()
                    .file(file)
                    .createdAt(instant)
                    .status(Thumbnail.ThumbnailStatus.PENDING)
                    .build();
            thumbnailRepository.save(thumbnail);
            Event<VideoUploadedEvent> event = new Event<VideoUploadedEvent>("VideoUploaded", "1.0", Instant.now(),
                    new VideoUploadedEvent(savedFile.getUuid(), metadata.getName(),
                            savedFile.getMetadata().getSize()));
            kafkaTemplate.send("video.events", event);
            return fileMapper.fileToFileDto(savedFile);
        } catch (Exception ex) {
            log.error("Exception occurred when trying to save the file:", ex);
            throw new RuntimeException(ex);
        }

    }

    @Transactional
    @Override
    public void deleteVideo(UUID uuid) {
        try {
            if (!fileRepository.existsById(uuid))
                throw new RuntimeException("Video not found!");
            File fileToDelete = fileRepository.findById(uuid).orElseThrow();
            if (fileToDelete.getMetadata() != null)
                metadataRepository.deleteById(fileToDelete.getMetadata().getId());
            if (fileToDelete.getPreview() != null) {
                if (fileToDelete.getPreview().getMetadata() != null) {
                    previewStorageService.delete(fileToDelete.getPreview().getMetadata().getName());
                    metadataRepository.deleteById(fileToDelete.getPreview().getMetadata().getId());
                }
                previewRepository.deleteById(fileToDelete.getPreview().getId());
            }
            if (fileToDelete.getThumbnail() != null) {
                if (fileToDelete.getThumbnail().getMetadata() != null) {
                    thumbnailStorageService.delete(fileToDelete.getThumbnail().getMetadata().getName());
                    metadataRepository.deleteById(fileToDelete.getThumbnail().getMetadata().getId());
                }
                thumbnailRepository.deleteById(fileToDelete.getThumbnail().getId());
            }
            videoStorageService.delete(fileToDelete.getMetadata().getName());
            fileRepository.deleteById(fileToDelete.getUuid());
        } catch (Exception e) {
            throw new RuntimeException("Couldn't delete Objects from storage", e);
        }
    }

    @Override
    public void addPreview(UUID uuid, PreviewUpdateEvent data) {
        if (!fileRepository.existsById(uuid))
            throw new RuntimeException("Video not found!");
        File file = fileRepository.findById(uuid).orElseThrow();
        // TODO: Make Change so that we can have multiple Preview records?
        // NOTE: for now Let's just Update the existing one even though method's name is
        // "add".
        if (file.getPreview() == null)
            throw new RuntimeException("No Preview Found!");
        Preview preview = file.getPreview();
        if (preview.getMetadata() == null) {
            Metadata metadata = metadataRepository
                    .save(Metadata.builder().mediaType(MediaType.APPLICATION_OCTET_STREAM).build());
            preview.setMetadata(metadata);
        }
        Metadata metadata = preview.getMetadata();
        if (data.getName() != null) {
            metadata.setName(data.getName());
        }
        if (data.getSize() > 0)
            metadata.setSize(data.getSize());
        if (data.getCreatedAt() instanceof Instant)
            preview.setCreatedAt(data.getCreatedAt());
        if (data.getStatus() != null && !data.getStatus().isBlank()) {
            switch (data.getStatus()) {
                case "processing" -> preview.setStatus(PreviewStatus.PROCESSING);
                case "ready" -> preview.setStatus(PreviewStatus.READY);
                case "failed" -> preview.setStatus(PreviewStatus.FAILED);
                default -> preview.setStatus(PreviewStatus.PENDING);
            }
        }
        preview.setMetadata(metadataRepository.save(metadata));
        previewRepository.save(preview);
    }

    @Override
    public void addThumbnail(UUID id, ThumbnailUpdateEvent data) {
        if (!fileRepository.existsById(id))
            throw new RuntimeException("Video not found!");
        File file = fileRepository.findById(id).orElseThrow();
        // TODO: Make Change so that we can have multiple Thumbnail records?
        // NOTE: for now Let's just Update the existing one even though method's name is
        // "add".
        if (file.getThumbnail() == null)
            throw new RuntimeException("No Thumbnail Found!");
        Thumbnail thumbnail = file.getThumbnail();
        if (thumbnail.getMetadata() == null) {
            Metadata metadata = metadataRepository
                    .save(Metadata.builder().mediaType(MediaType.IMAGE_PNG).build());
            thumbnail.setMetadata(metadata);
        }
        Metadata metadata = thumbnail.getMetadata();
        if (data.getName() != null) {
            metadata.setName(data.getName());
        }
        if (data.getSize() > 0)
            metadata.setSize(data.getSize());
        if (data.getCreatedAt() instanceof Instant)
            thumbnail.setCreatedAt(data.getCreatedAt());
        if (data.getStatus() != null && !data.getStatus().isBlank()) {
            switch (data.getStatus()) {
                case "processing" -> thumbnail.setStatus(ThumbnailStatus.PROCESSING);
                case "ready" -> thumbnail.setStatus(ThumbnailStatus.READY);
                case "failed" -> thumbnail.setStatus(ThumbnailStatus.FAILED);
                default -> thumbnail.setStatus(ThumbnailStatus.PENDING);
            }
        }
        thumbnail.setMetadata(metadataRepository.save(metadata));
        thumbnailRepository.save(thumbnail);
    }

    @Override
    public List<FileDto> getAllInfo() {
        return fileRepository.findAll().stream().map(fileMapper::fileToFileDto).toList();
    }

    @Override
    public FileDto getInfoById(UUID uuid) {
        return fileMapper.fileToFileDto(fileRepository.findById(uuid).orElseThrow());
    }

    @Override
    public MetadataDto getVideoMetadata(UUID uuid) {
        File file = fileRepository.findById(uuid).orElseThrow();
        return metadataMapper.metadataToMetadataDto(file.getMetadata());
    }

    @Override
    public MetadataDto getPreviewMetadata(UUID uuid) {
        File file = fileRepository.findById(uuid).orElseThrow();
        return metadataMapper.metadataToMetadataDto(file.getPreview().getMetadata());
    }

    @Override
    public MetadataDto getThumbnailMetadata(UUID uuid) {
        File file = fileRepository.findById(uuid).orElseThrow();
        return metadataMapper.metadataToMetadataDto(file.getThumbnail().getMetadata());
    }

    @Transactional
    @Override
    public void requestPreviewGeneration(UUID uuid) {
        // TODO: Validate request. i.e check if Video exists, size etc.
        File file = fileRepository.findById(uuid).orElseThrow();
        Event<VideoUploadedEvent> event = new Event<VideoUploadedEvent>("VideoUploaded", "1.0", Instant.now(),
                new VideoUploadedEvent(uuid, file.getMetadata().getName(), file.getMetadata().getSize()));
        CompletableFuture<SendResult<String, Event<?>>> future = kafkaTemplate.send("video.events",
                event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[ {} ] with offset=[ {} ]", event.getPayload().fileName(),
                        result.getRecordMetadata().offset());
            } else {
                log.info("Unable to send message=[ {} ] due to: {}", event.getPayload().fileName(), ex.getMessage());
            }
        });
    }

}
