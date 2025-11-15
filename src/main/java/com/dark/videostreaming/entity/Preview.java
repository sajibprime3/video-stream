package com.dark.videostreaming.entity;

import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @JoinColumn(name = "file_uuid", referencedColumnName = "uuid")
    private File file;

    @OneToOne
    Metadata metadata;

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
