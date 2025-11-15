package com.dark.videostreaming.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID uuid;

    String title;

    LocalDate publishDate;

    @OneToOne
    Metadata metadata;

    @OneToOne(mappedBy = "file")
    @JoinColumn(name = "preview_id", referencedColumnName = "id")
    Preview preview;

    @OneToOne(mappedBy = "file")
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "id")
    Thumbnail thumbnail;

}
