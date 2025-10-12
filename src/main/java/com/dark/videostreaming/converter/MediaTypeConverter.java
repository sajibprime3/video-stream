package com.dark.videostreaming.converter;

import jakarta.persistence.AttributeConverter;

import org.springframework.http.MediaType;

public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType mediaType) {
        return mediaType != null ? mediaType.toString() : null;
    }

    @Override
    public MediaType convertToEntityAttribute(String dbData) {
        MediaType mediaType = dbData != null ? MediaType.parseMediaType(dbData) : null;
        return mediaType;
    }

}
