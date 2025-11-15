package com.dark.videostreaming.service;

import java.util.List;
import java.util.UUID;

import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.dto.MetadataDto;
import com.dark.videostreaming.event.model.PreviewUpdateEvent;
import com.dark.videostreaming.event.model.ThumbnailUpdateEvent;

import org.springframework.web.multipart.MultipartFile;

public interface VideoService {

    FileDto save(MultipartFile video, String title);

    void deleteVideo(UUID uuid);

    void addPreview(UUID uuid, PreviewUpdateEvent data);

    void addThumbnail(UUID uuid, ThumbnailUpdateEvent data);

    List<FileDto> getAllInfo();

    FileDto getInfoById(UUID uuid);

    MetadataDto getVideoMetadata(UUID uuid);

    MetadataDto getPreviewMetadata(UUID uuid);

    MetadataDto getThumbnailMetadata(UUID uuid);

    void requestPreviewGeneration(UUID uuid);

}
