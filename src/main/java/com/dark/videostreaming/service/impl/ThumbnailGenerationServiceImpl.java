package com.dark.videostreaming.service.impl;

import com.dark.videostreaming.entity.File;
import com.dark.videostreaming.entity.FileMetadata;
import com.dark.videostreaming.entity.Thumbnail;
import com.dark.videostreaming.event.ThumbnailCreationEvent;
import com.dark.videostreaming.repository.FileRepository;
import com.dark.videostreaming.repository.ThumbnailRepository;
import com.dark.videostreaming.service.ThumbnailGenerationService;
import com.dark.videostreaming.service.ThumbnailStorageService;
import com.dark.videostreaming.service.VideoStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
@Service
public class ThumbnailGenerationServiceImpl implements ThumbnailGenerationService {
    
    public final FileRepository fileRepository;
    public final VideoStorageService videoStorageService;
    public final ThumbnailRepository thumbnailRepository;
    public final ThumbnailStorageService thumbnailStorageService;
    
    
    private final Path temp = Paths.get(System.getProperty("user.dir")).resolve("tmpThumb");
    
    @Async
    @TransactionalEventListener
    @Override
    public void generateThumbnail(ThumbnailCreationEvent event) {
        File video = fileRepository.findById(event.getFileId()).orElseThrow();
        generateAndStoreThumbnail(video.getThumbnail(), video.getMetadata());
        
    }
    
    @Transactional
    private void generateAndStoreThumbnail(Thumbnail thumbnail, FileMetadata metadata) {
        try {
            thumbnail.setStatus(Thumbnail.ThumbnailStatus.PROCESSING);
            thumbnailRepository.save(thumbnail);
            if (Files.notExists(temp, LinkOption.NOFOLLOW_LINKS)) Files.createDirectory(temp);
            Path tempInput = temp.resolve(metadata.getUuid() + ".mp4");
            if (Files.notExists(tempInput, LinkOption.NOFOLLOW_LINKS)) Files.createFile(tempInput);
            try (InputStream is = videoStorageService.getInputStream(metadata.getUuid().toString(), 0, metadata.getSize());
                 OutputStream os = Files.newOutputStream(tempInput, StandardOpenOption.TRUNCATE_EXISTING)) {
                is.transferTo(os);
            }
            double duration = getVideoDuration(tempInput.toFile().getAbsolutePath());
            Path tempDir = temp.resolve("gen");
            if (Files.notExists(tempDir, LinkOption.NOFOLLOW_LINKS)) Files.createDirectory(tempDir);
            try {
                Path output = tempDir.resolve("thumbnail.png");
                generateThumbnail(tempInput.toString(), output.toString(), duration);
                
                long size = output.toFile().length();
                try (InputStream inputStream = Files.newInputStream(output)) {
                    thumbnailStorageService.save(inputStream, thumbnail.getName(), size);
                    thumbnail.setSize(size);
                    thumbnail.setStatus(Thumbnail.ThumbnailStatus.READY);
                    thumbnailRepository.save(thumbnail);
                }
            } finally {
                FileUtils.deleteDirectory(tempDir.toFile());
                Files.deleteIfExists(tempInput);
                FileUtils.deleteDirectory(temp.toFile());
            }
        } catch (IOException e) {
            log.warn("Failed to completely delete temp dir, but ignoring.", e);
        } catch (Exception e) {
            thumbnail.setStatus(Thumbnail.ThumbnailStatus.FAILED);
            thumbnailRepository.save(thumbnail);
            throw new RuntimeException("Failed to create Preview: ", e);
        }
    }
    
    private double getVideoDuration(String filePath) throws IOException, InterruptedException {
        
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                filePath
        );
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String durationStr = reader.readLine();
            process.waitFor();
            
            if (durationStr == null || durationStr.isBlank()) {
                
                BufferedReader bf = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                log.error("Couldn't figure out Duration of the Video: {}", bf.readLine());
                throw new RuntimeException("Unable to read Duration");
            }
            return Double.parseDouble(durationStr);
        }
    }
    
    private void generateThumbnail(String input, String output, double duration) throws Exception {
        String startStamp = String.format(Locale.US, "%.2f", duration / 2);
        int scanLength = 5;
        List<String> command = List.of(
                "ffmpeg",
                "-ss", startStamp,
                "-i", input,
                "-t", String.valueOf(scanLength),
                "-vf", "thumbnail",
                "-frames:v", "1",
                "-an",
                "-preset", "ultrafast",
                output);
        new ProcessBuilder(command).inheritIO().start().waitFor();
    }
}

