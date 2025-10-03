package com.dark.videostreaming.mapper;

import com.dark.videostreaming.dto.ThumbnailDto;
import com.dark.videostreaming.entity.Thumbnail;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ThumbnailMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "status", expression = "java(thumbnail.getStatus().name())")
    ThumbnailDto thumbnailToThumbnailDto(Thumbnail thumbnail);
}
