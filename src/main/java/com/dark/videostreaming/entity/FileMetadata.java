package com.dark.videostreaming.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FileMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    
    private long size;
    
    private String HttpContentType;
    
}
