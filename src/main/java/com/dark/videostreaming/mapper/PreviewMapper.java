package com.dark.videostreaming.mapper;

import com.dark.videostreaming.dto.PreviewDto;
import com.dark.videostreaming.entity.Preview;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PreviewMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "status", expression = "java(preview.getStatus().name())")
    PreviewDto previewToPreviewDto(Preview preview);

}
