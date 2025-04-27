package com.dark.videostreaming.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Preview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @OneToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private File file;
    
    private String name;
    
    private long size;
    
    private Duration duration;
    
    private Instant createdAt;
    
    
    @Enumerated(EnumType.STRING)
    private PreviewStatus status;
    
    public enum PreviewStatus {
        PENDING,
        PROCESSING,
        READY,
        FAILED

    }
}

