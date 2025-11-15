package com.dark.videostreaming.mapper;

import com.dark.videostreaming.dto.MetadataDto;
import com.dark.videostreaming.entity.Metadata;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MetadataMapper {

    @Mapping(target = "name", source = "name")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "contentType", expression = "java(metadata.toString())")
    MetadataDto metadataToMetadataDto(Metadata metadata);

}
