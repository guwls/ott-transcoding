package com.example.worker.service;

import com.example.worker.ffmpeg.*;
import com.example.worker.repo.VideoRepository;
import com.example.worker.storage.StorageService;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.worker.ffmpeg.FfprobeUtil;
import java.nio.file.*;
import java.util.*;

import static com.example.worker.util.ResourceGuards.rmQuietly;
import static com.example.worker.util.ResourceGuards.tempDir;

@Service
public class HlsMultiPipeline {

    private static final Logger log = LoggerFactory.getLogger(HlsMultiPipeline.class);

    private final VideoRepository videos;
    private final StorageService storage;
    private final FFmpegRunner ffmpeg = new FFmpegRunner();
    private final boolean failOnAnyVariant;

    public HlsMultiPipeline(VideoRepository videos, StorageService storage,
                            @Value("${app.worker.fail-on-any-variant:false}") boolean failOnAnyVariant) {
        this.videos = videos; this.storage = storage; this.failOnAnyVariant = failOnAnyVariant;
    }

    /** targetPrefix 예: videos/{videoId}/hls/ → 그 아래에 480p/720p/1080p + master.m3u8 생성/업로드 */
    public void run(Long videoId, String targetPrefix, List<String> variantNames) {
        var video = videos.findById(videoId).orElseThrow(() -> new IllegalStateException("VIDEO_NOT_FOUND:" + videoId));
        var inputKey = video.getOriginalKey();

        Path input = null; Path outRoot = null;
        var presets = PresetFactory.resolveAll(variantNames);

        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        double dur = FfprobeUtil.durationSeconds(input);                  // 초
        long timeoutSec = Math.min( (long)(dur * 6) + 120, 60 * 30 );     // ≈ 실시간*6 + 여유, 상한 30분

        try {
            input = storage.downloadToTemp(inputKey);    // 내부에서 tempFile 사용
            outRoot = tempDir("hls-");

            for (HlsPreset p : presets) {
                var vdir = outRoot.resolve(p.name()); // 예: /tmp/hls-xxxx/720p
                ffmpeg.runVariant(input, vdir, p, timeoutSec);
            }

            if (succeeded.isEmpty())
                throw new RuntimeException("ALL_VARIANTS_FAILED");

            // 마스터 m3u8 생성
            var master = outRoot.resolve("master.m3u8");
            HlsMasterBuilder.writeMaster(master, presets.stream().filter(p -> succeeded.contains(p.name())).toList());

            // 업로드: targetPrefix 아래 전체 업로드 (root에 master.m3u8, 각 변형은 하위 폴더)
            storage.uploadDir(targetPrefix, outRoot);

            log.info("[HLS] uploaded master + variants ok. success={} failed={}", succeeded, failed);
        } finally {
            rmQuietly(input);
            rmQuietly(outRoot);
        }
    }

}
