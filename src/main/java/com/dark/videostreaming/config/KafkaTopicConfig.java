package com.dark.videostreaming.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic videoEvents() {
        return TopicBuilder.name("video.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic previewEvents() {
        return TopicBuilder.name("video.preview.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
