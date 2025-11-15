package com.dark.videostreaming.controller.api;

import java.util.List;
import java.util.UUID;

import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.dto.MetadataDto;
import com.dark.videostreaming.service.VideoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoRestController {

    private final VideoService videoService;

    @Value("${app.streaming.default-chunk-size}")
    public Integer defaultChunkSize;

    @GetMapping
    public ResponseEntity<List<FileDto>> getAllVideoInfo() {
        return ResponseEntity.ok(videoService.getAllInfo());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<FileDto> getVideoInfoById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(videoService.getInfoById(uuid));
    }

    @PostMapping
    public ResponseEntity<FileDto> save(@RequestParam("file") MultipartFile file, @RequestParam String title) {
        FileDto dto = videoService.save(file, title);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<String> deleteVideo(@PathVariable UUID uuid) {
        videoService.deleteVideo(uuid);
        return ResponseEntity.ok("Request sent.");
    }

    @GetMapping("/{uuid}/generate-preview")
    public ResponseEntity<String> generatePreview(@PathVariable UUID uuid) {
        videoService.requestPreviewGeneration(uuid);
        return ResponseEntity.ok("Request sent.");
    }

    @GetMapping("{uuid}/metadata")
    public ResponseEntity<MetadataDto> getMetadata(@PathVariable UUID uuid) {
        MetadataDto metadata = videoService.getVideoMetadata(uuid);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("{uuid}/preview/metadata")
    public ResponseEntity<MetadataDto> getPreviewMetadata(@PathVariable UUID uuid) {
        MetadataDto metadata = videoService.getPreviewMetadata(uuid);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("{uuid}/thumbnail/metadata")
    public ResponseEntity<MetadataDto> getThumbnailMetadata(@PathVariable UUID uuid) {
        MetadataDto metadata = videoService.getThumbnailMetadata(uuid);
        return ResponseEntity.ok(metadata);
    }
}
