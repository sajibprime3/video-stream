package com.dark.videostreaming.dto;

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

    long id;

    String title;

    String publishTimestamp;

    String fileUUID;

    PreviewDto preview;

    ThumbnailDto thumbnail;
}
