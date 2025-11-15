package com.dark.videostreaming.mapper;

import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.entity.File;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { PreviewMapper.class, ThumbnailMapper.class })
public interface FileMapper {

    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "publishTimestamp", expression = "java(com.dark.videostreaming.util.TimeUtil.timeSince(file.getPublishDate()))")
    @Mapping(target = "preview", source = "preview")
    @Mapping(target = "thumbnail", source = "thumbnail")
    FileDto fileToFileDto(File file);

}
