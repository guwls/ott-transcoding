package com.example.worker.service;

import com.example.worker.ffmpeg.FfprobeUtil;
import com.example.worker.storage.StorageService;
import com.example.worker.repo.VideoRepository;
import com.example.worker.repo.ThumbnailRepository;
import com.example.worker.thumb.ThumbTimes;
import com.example.worker.thumb.ThumbnailGenerator;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;

import static com.example.worker.util.ResourceGuards.rmQuietly;
import static com.example.worker.util.ResourceGuards.tempDir;

@Service
public class ThumbnailPipeline {
    private static final Logger log = LoggerFactory.getLogger(ThumbnailPipeline.class);

    private final VideoRepository videos;
    private final ThumbnailRepository thumbs;
    private final StorageService storage;
    private final ThumbnailGenerator gen = new ThumbnailGenerator();

    public ThumbnailPipeline(VideoRepository videos, ThumbnailRepository thumbs, StorageService storage) {
        this.videos = videos; this.thumbs = thumbs; this.storage = storage;
    }

    /** 업로드 경로: videos/{videoId}/thumbs/ */
    @Value("${app.worker.simulate-all:false}") boolean simulateAll;
    public void run(Long videoId) {
        //비디오 조회 -> DB에 없으면 즉시 실패
        var video = videos.findById(videoId).orElseThrow(() -> new IllegalStateException("VIDEO_NOT_FOUND:"+videoId));
        String inputKey = video.getOriginalKey();

        Path input = null; Path outDir = null;
        try {
            //원본 다운로드 & 썸네일 시점 결정
            input = storage.downloadToTemp(inputKey); //MinIO -> 로컬 temp 파일
            double dur = FfprobeUtil.durationSeconds(input); //ffprobe로 길이(초)
            List<Integer> times = ThumbTimes.pick(dur); //예: [5, 15, 30, ...]

            //로컬 임시 출력 디렉토리에서 썸네일 생성
            //각 시점마다 thumb_000010.jpg 같은 규칙 이름으로 생성
            //maxWidth=1280 → 긴 변이 1280을 넘지 않도록 스케일
            outDir = tempDir("thumbs-");
            List<Path> files = new ArrayList<>();
            for (int t : times) {
                files.add(gen.captureAt(input, outDir, t, 1280));
            }

            //원격 스토리지 업로드
            //결과 키: videos/{videoId}/thumbs/thumb_000010.jpg 형태
            //StorageService.uploadDir가 디렉토리 내부 파일을 전부 올림
            String prefix = "videos/%d/thumbs".formatted(videoId);
            storage.uploadDir(prefix, outDir);

            // DB 저장 (object_key는 prefix/파일명)
            var toSave = files.stream().map(p -> {
                String name = p.getFileName().toString(); // thumb_000010.jpg
                String key = (prefix + "/" + name).replaceAll("//+", "/");
                int sec = Integer.parseInt(name.substring("thumb_".length(), "thumb_".length()+6));
                return new com.example.worker.domain.Thumbnail(videoId, key, sec, null, null);
            }).toList();
            thumbs.saveAll(toSave);

            log.info("[THUMBS] created={} for videoId={}", files.size(), videoId);
        } catch (Exception e) {
            throw new RuntimeException("THUMB_PIPELINE_FAILED", e);
        } finally {
            rmQuietly(input);
            rmQuietly(outDir);
        }

        if (simulateAll) { try { Thread.sleep(5); } catch (InterruptedException ignored) {} return; }
    }

    private void safeDelete(Path p){ if (p==null) return; try{ Files.deleteIfExists(p);}catch(Exception ignored){} }
    private void safeDeleteDir(Path d){ if (d==null) return; try{ Files.walk(d).sorted((a,b)->b.compareTo(a)).forEach(x->{ try{ Files.deleteIfExists(x);}catch(Exception ignored){} }); } catch(Exception ignored){} }
}