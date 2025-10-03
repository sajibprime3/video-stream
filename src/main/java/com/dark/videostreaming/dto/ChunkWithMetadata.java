package com.dark.videostreaming.dto;

import org.springframework.http.MediaType;

public record ChunkWithMetadata(
        String name,
        long size,
        MediaType contentType,
        byte[] chunk) {
}
