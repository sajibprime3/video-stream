package com.dark.videostreaming.service;

import java.util.List;
import java.util.UUID;

import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.util.Range;

import org.springframework.web.multipart.MultipartFile;

public interface VideoService {

    FileDto save(MultipartFile video, String title);

    void deleteVideo(long id);

    ChunkWithMetadata fetchVideoChunk(UUID uuid, Range range);

    ChunkWithMetadata fetchPreviewChunk(long id, Range range);

    ChunkWithMetadata fetchThumbnail(long id);

    List<FileDto> getAllInfo();

    FileDto getInfoById(long id);

    void requestPreviewGeneration(long id);

}
