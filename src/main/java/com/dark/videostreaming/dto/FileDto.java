package com.dark.videostreaming.dto;

import java.util.UUID;

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
public class FileDto {

    UUID uuid;

    String title;

    String publishTimestamp;

    PreviewDto preview;

    ThumbnailDto thumbnail;
}
