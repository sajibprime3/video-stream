package com.dark.videostreaming.mapper;

import com.dark.videostreaming.dto.FileDto;
import com.dark.videostreaming.entity.File;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "publishTimestamp", expression = "java(com.dark.videostreaming.util.TimeUtil.timeSince(file.getPublishDate()))")
    @Mapping(target = "fileUUID", expression = "java(file.getMetadata().getUuid().toString())")
    FileDto fileToFileDto(File file);
    
   
    
    
}
