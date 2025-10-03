package com.dark.videostreaming.controller.api;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.dark.videostreaming.controller.HttpConstants;
import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.service.VideoService;
import com.dark.videostreaming.util.Range;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/video")
public class VideoRestController {

    private final VideoService videoService;

    @Value("${app.streaming.default-chunk-size}")
    public Integer defaultChunkSize;

    @PostMapping
    public ResponseEntity<FileDto> save(@RequestParam("file") MultipartFile file, @RequestParam String title) {
        FileDto dto = videoService.save(file, title);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVideo(@PathVariable long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok("Request sent.");
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<byte[]> getVideoChunk(
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
            @PathVariable UUID uuid) {
        Range parsedRange = Range.parseHttpRangeString(range, defaultChunkSize);
        ChunkWithMetadata chunkWithMetadata = videoService.fetchVideoChunk(uuid, parsedRange);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(chunkWithMetadata.contentType())
                .header(HttpHeaders.ACCEPT_RANGES, HttpConstants.ACCEPTS_RANGES_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, calculateContentLengthHeader(parsedRange, chunkWithMetadata.size()))
                .header(HttpHeaders.CONTENT_RANGE, constructContentRangeHeader(parsedRange, chunkWithMetadata.size()))
                .body(chunkWithMetadata.chunk());
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> getPreviewChunk(
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
            @PathVariable long id) {
        Range parsedRange = Range.parseHttpRangeString(range, defaultChunkSize);
        ChunkWithMetadata chunkWithMetadata = videoService.fetchPreviewChunk(id, parsedRange);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(chunkWithMetadata.contentType())
                .header(HttpHeaders.ACCEPT_RANGES, HttpConstants.ACCEPTS_RANGES_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, calculateContentLengthHeader(parsedRange, chunkWithMetadata.size()))
                .header(HttpHeaders.CONTENT_RANGE, constructContentRangeHeader(parsedRange, chunkWithMetadata.size()))
                .body(chunkWithMetadata.chunk());
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable long id) {
        ChunkWithMetadata fetchedChunkWithMetadata = videoService.fetchThumbnail(id);
        return ResponseEntity.ok()
                .contentType(fetchedChunkWithMetadata.contentType())
                .cacheControl(CacheControl.maxAge(10, TimeUnit.DAYS))
                .body(fetchedChunkWithMetadata.chunk());
    }

    @GetMapping("/generate-preview/{id}")
    public ResponseEntity<String> generatePreview(@PathVariable long id) {
        videoService.requestPreviewGeneration(id);
        return ResponseEntity.ok("Request sent.");
    }

    @GetMapping("/info")
    public ResponseEntity<List<FileDto>> getAllVideoInfo() {
        return ResponseEntity.ok(videoService.getAllInfo());
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<FileDto> getVideoInfoById(@PathVariable long id) {
        return ResponseEntity.ok(videoService.getInfoById(id));
    }

    private String calculateContentLengthHeader(Range range, long fileSize) {
        return String.valueOf(range.getRangeEnd(fileSize) - range.getRangeStart() + 1);
    }

    private String constructContentRangeHeader(Range range, long fileSize) {
        return "bytes " + range.getRangeStart() + "-" + range.getRangeEnd(fileSize) + "/" + fileSize;
    }

}
