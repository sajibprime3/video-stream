package com.dark.videostreaming.event.consumer;

import com.dark.videostreaming.event.Event;
import com.dark.videostreaming.event.model.ThumbnailUpdateEvent;
import com.dark.videostreaming.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ThumbnailListener {

    private final ObjectMapper objectMapper;
    private final VideoService videoService;

    @KafkaListener(topics = "video.thumbnail.events")
    public void listen(Event<?> event) {
        switch (event.getEventType()) {
            case "ThumbnailUpdateEvent" ->
                thumbnailUpdateHandler(objectMapper.convertValue(event.getPayload(), ThumbnailUpdateEvent.class));
            default -> {
            }
        }
    }

    private void thumbnailUpdateHandler(ThumbnailUpdateEvent eventData) {
        videoService.addThumbnail(eventData.getVideoId(), eventData);
    }

}
