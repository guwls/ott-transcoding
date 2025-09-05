package com.example.worker.ffmpeg;

public record HlsPreset(
        String name,         // "480p" / "720p" / "1080p"
        int width,           // 마스터 m3u8의 RESOLUTION 표기를 위해 고정폭
        int height,          // ffmpeg scale -2:<height>
        String vBitrate,     // 예: "2800k"
        String vMaxrate,     // 예: "3000k"
        String vBufsize,     // 예: "5600k"
        String aBitrate      // 예: "128k"
) { }
