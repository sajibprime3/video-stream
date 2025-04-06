package com.dark.videostreaming.dto;

import com.dark.videostreaming.entity.FileMetadata;

public record ChunkWithMetadata(
        FileMetadata metadata,
        byte[] chunk
) {}
