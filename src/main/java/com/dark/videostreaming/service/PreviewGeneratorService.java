package com.dark.videostreaming.service;

import com.dark.videostreaming.event.PreviewCreationEvent;

public interface PreviewGeneratorService {
    
    void generatePreview(PreviewCreationEvent event);
    
}
