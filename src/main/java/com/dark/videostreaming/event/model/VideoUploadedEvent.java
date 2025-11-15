package com.dark.videostreaming.event.model;

import java.util.UUID;

public record VideoUploadedEvent(
        UUID videoId,
        String fileName,
        long fileSize) {
}
