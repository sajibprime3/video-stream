package com.dark.videostreaming.service;

import com.dark.videostreaming.event.ThumbnailCreationEvent;

public interface ThumbnailGenerationService {
    void generateThumbnail(ThumbnailCreationEvent event);
}
