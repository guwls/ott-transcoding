package com.example.app.controller;

import com.example.app.repo.ThumbnailRepository;
import com.example.app.repo.VideoRepository;
import com.example.app.service.ObjectUrlService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/videos")
public class VideoQueryController {

    private final VideoRepository videos;
    private final ThumbnailRepository thumbs;
    private final ObjectUrlService url;

    public VideoQueryController(VideoRepository videos, ThumbnailRepository thumbs, ObjectUrlService url) {
        this.videos = videos; this.thumbs = thumbs; this.url = url;
    }

    public record ThumbDto(String key, int timeSec, String url) {}
    public record HlsDto(String masterKey, String masterUrl, List<String> variants) {}
    public record VideoDetail(Long id, String title, String originalKey, HlsDto hls, List<ThumbDto> thumbnails) {}

    @GetMapping("/{id}")
    public VideoDetail get(@PathVariable Long id) {
        var v = videos.findById(id).orElseThrow(() -> new NoSuchElementException("video not found: "+id));

        // HLS 경로 규칙과 맞추기 (Day 6 기준)
        String hlsPrefix = "videos/%d/hls/".formatted(id);
        String masterKey = hlsPrefix + "master.m3u8";
        String masterUrl = url.presignGet(masterKey, 60);
        List<String> variants = List.of("480p","720p","1080p");

        var tlist = thumbs.findByVideoIdOrderByTimeSecAsc(id).stream()
                .map(t -> new ThumbDto(t.getObjectKey(), t.getTimeSec(), url.presignGet(t.getObjectKey(), 60)))
                .toList();

        return new VideoDetail(v.getId(), v.getTitle(), v.getOriginalKey(),
                new HlsDto(masterKey, masterUrl, variants), tlist);
    }
}