package com.dark.videostreaming.dto;

public record ChunkWithMetadata(
        String name,
        long size,
        String HttpContentType,
        byte[] chunk
) {}
