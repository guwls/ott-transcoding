package com.example.worker.service;

import com.example.worker.ffmpeg.FFmpegRunner;
import com.example.worker.repo.VideoRepository;
import com.example.worker.storage.StorageService;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class Hls720Pipeline {
    private static final Logger log = LoggerFactory.getLogger(Hls720Pipeline.class);

    private final VideoRepository videos;
    private final StorageService storage;
    private final FFmpegRunner ffmpeg = new FFmpegRunner();

    public Hls720Pipeline(VideoRepository videos, StorageService storage) {
        this.videos = videos; this.storage = storage;
    }

    /** targetPrefix 예: videos/{videoId}/hls/  → 그 아래에 720p/index.m3u8 업로드 */
    public void run(Long videoId, String targetPrefix) throws IOException {
        var video = videos.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("VIDEO_NOT_FOUND:" + videoId));
        var inputKey = video.getOriginalKey();

        Path input = null; Path outDir = null;
        try {
            input = storage.downloadToTemp(inputKey);
            outDir = Files.createTempDirectory("hls720-");

            // ffmpeg 실행 → outDir 안에 index.m3u8 + segment_XXX.ts 생성
            ffmpeg.runHls720p(input, outDir);

            // 업로드: {targetPrefix}/720p/
            String prefix = (targetPrefix.endsWith("/") ? targetPrefix : targetPrefix + "/") + "720p";
            storage.uploadDir(prefix, outDir);

            log.info("[HLS720] uploaded to prefix={}", prefix);
        } finally {
            // 로컬 파일 정리
            safeDelete(input);
            safeDeleteDir(outDir);
        }
    }

    private void safeDelete(Path p) {
        if (p == null) return;
        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
    }
    private void safeDeleteDir(Path dir) {
        if (dir == null) return;
        try { Files.walk(dir).sorted((a,b)->b.compareTo(a)).forEach(path -> { try { Files.deleteIfExists(path);}catch(Exception ignored){} }); }
        catch (Exception ignored) {}
    }
}
