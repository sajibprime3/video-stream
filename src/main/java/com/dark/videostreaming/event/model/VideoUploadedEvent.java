package com.dark.videostreaming.event.model;

public record VideoUploadedEvent(
        long videoId,
        String fileName,
        long fileSize) {
}
