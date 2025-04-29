package com.dark.videostreaming.util;

import com.dark.videostreaming.service.MinioStorageService;

import java.io.InputStream;

public class ChunkReader {

    public static byte[] read(String objectName, Range range, long fileSize, MinioStorageService storageService) {
        long startPosition = range.getRangeStart();
        long endPosition = range.getRangeEnd(fileSize);
        int chunkSize = (int) (endPosition - startPosition + 1);
        try(InputStream inputStream = storageService.getInputStream(objectName, startPosition, chunkSize)) {
            return inputStream.readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't Read Object from Storage. Error: ",ex);
        }
    }
    
}
