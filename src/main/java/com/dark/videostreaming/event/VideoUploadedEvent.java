package com.dark.videostreaming.event;

public record VideoUploadedEvent(
        long videoId,
        String fileName) {
}
