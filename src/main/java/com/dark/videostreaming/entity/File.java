package com.dark.videostreaming.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    
    String title;
    
    LocalDate publishDate;
    
    @OneToOne
    FileMetadata metadata;
    
    
}
