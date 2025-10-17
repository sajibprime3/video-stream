package com.dark.videostreaming.event.consumer;

import com.dark.videostreaming.event.Event;
import com.dark.videostreaming.event.model.PreviewUpdateEvent;
import com.dark.videostreaming.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PreviewListener {

    private final ObjectMapper objectMapper;
    private final VideoService videoService;

    @KafkaListener(topics = "video.preview.events")
    public void listen(Event<?> event) {
        switch (event.getEventType()) {
            case "PreviewUpdateEvent":
                PreviewUpdateEvent eventData = objectMapper.convertValue(event.getPayload(), PreviewUpdateEvent.class);
                previewUpdateHandler(eventData);
                break;

            default:
                break;
        }
    }

    private void previewUpdateHandler(PreviewUpdateEvent eventData) {
        videoService.addPreview(eventData.getVideoId(), eventData);
    }

}
