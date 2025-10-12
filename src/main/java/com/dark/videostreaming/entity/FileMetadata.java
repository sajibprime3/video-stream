package com.dark.videostreaming.entity;

import java.util.UUID;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.dark.videostreaming.converter.MediaTypeConverter;

import org.springframework.http.MediaType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Convert(converter = MediaTypeConverter.class)
    private MediaType mediaType;

}
