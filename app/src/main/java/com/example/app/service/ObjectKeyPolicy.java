package com.example.app.service;

import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class ObjectKeyPolicy {
    public String uploadKeyFor(Long videoId, String ext) {
        var d = LocalDate.now();
        return "uploads/%d/%02d/%02d/%d.%s".formatted(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), videoId, ext);
    }
    public String hlsPrefix(Long videoId) { return "videos/%d/hls/".formatted(videoId); }
    public String thumbsPrefix(Long videoId) { return "videos/%d/thumbs".formatted(videoId); }
    public String masterKey(Long videoId) { return hlsPrefix(videoId) + "master.m3u8"; }
}