package com.dark.videostreaming.controller.api;

import com.dark.videostreaming.controller.HttpConstants;
import com.dark.videostreaming.dto.ChunkWithMetadata;
import com.dark.videostreaming.service.VideoService;
import com.dark.videostreaming.util.Range;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/video")
public class VideoRestController {
    
    private final VideoService videoService;
    
    @Value("${app.streaming.default-chunk-size}")
    public Integer defaultChunkSize;
    
    @PostMapping
    public ResponseEntity<UUID> save(@RequestParam("file") MultipartFile file) {
        UUID fileUuid = videoService.save(file);
        return ResponseEntity.ok(fileUuid);
    }
    
    @GetMapping("/{uuid}")
    public ResponseEntity<byte[]> getChunk(
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
            @PathVariable UUID uuid
    ) {
        Range parsedRange = Range.parseHttpRangeString(range, defaultChunkSize);
        ChunkWithMetadata chunkWithMetadata = videoService.fetchChunk(uuid, parsedRange);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, chunkWithMetadata.metadata().getHttpContentType())
                .header(HttpHeaders.ACCEPT_RANGES, HttpConstants.ACCEPTS_RANGES_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, calculateContentLengthHeader(parsedRange, chunkWithMetadata.metadata().getSize()))
                .header(HttpHeaders.CONTENT_RANGE, constructContentRangeHeader(parsedRange, chunkWithMetadata.metadata().getSize()))
                .body(chunkWithMetadata.chunk());
    }
    
    private String calculateContentLengthHeader(Range range, long fileSize) {
        return String.valueOf(range.getRangeEnd(fileSize) - range.getRangeStart() + 1);
    }
    
    private String constructContentRangeHeader(Range range, long fileSize) {
        return "bytes " + range.getRangeStart() + "-" + range.getRangeEnd(fileSize) + "/" + fileSize;
    }
    
    
}
