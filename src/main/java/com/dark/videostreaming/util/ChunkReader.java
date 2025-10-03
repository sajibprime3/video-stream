package com.dark.videostreaming.util;

import java.io.InputStream;

import com.dark.videostreaming.service.MinioStorageService;

public class ChunkReader {

    /**
     * Returns A part of object from the specified MinioStorageService as an array
     * of byte. The part is specified via {@link Range}.
     * To get a full object instead
     * Use overloaded method without {@link Range} param.
     * 
     * @param objectName     The Name of the Object.
     * @param range          The part specified as a {@link Range}.
     * @param fileSize       The size of the File/Object.
     * @param storageService The service which will be used to Read the Object from.
     * @return The full Object in byte[].
     */
    public static byte[] read(String objectName, Range range, long fileSize, MinioStorageService storageService) {
        long startPosition = range.getRangeStart();
        long endPosition = range.getRangeEnd(fileSize);
        int chunkSize = (int) (endPosition - startPosition + 1);
        try (InputStream inputStream = storageService.getInputStream(objectName, startPosition, chunkSize)) {
            return inputStream.readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't Read Object from Storage. Error: ", ex);
        }
    }

    /**
     * Returns A full object from the specified MinioStorageService as an array of
     * byte.
     * to get only a part of the object see the overloaded method with a
     * {@link Range} param.
     * 
     * @param objectName     The Name of the Object.
     * @param fileSize       The size of the File/Object.
     * @param storageService The service which will be used to Read the Object from.
     * @return The full Object in byte[].
     */
    public static byte[] read(String objectName, long fileSize, MinioStorageService storageService) {
        long startPosition = 0;
        try (InputStream inputStream = storageService.getInputStream(objectName, startPosition, fileSize)) {
            return inputStream.readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't Read Object from Storage. Error: ", ex);
        }
    }
}
