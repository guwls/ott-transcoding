package com.example.worker.ffmpeg;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class FFmpegRunner {

    public void runVariant(Path inputMp4, Path outDir, HlsPreset p) {
        try { Files.createDirectories(outDir); }
        catch (IOException e) { throw new RuntimeException("PREPARE_OUTDIR_FAILED:" + outDir, e); }

        var seg = outDir.resolve("segment_%03d.ts").toString();
        var out = outDir.resolve("index.m3u8").toString();

        List<String> cmd = List.of(
                "ffmpeg","-y",
                "-i", inputMp4.toAbsolutePath().toString(),
                "-vf","scale=-2:"+p.height(),          // 가로 비율 유지, 높이 고정
                "-c:v","h264",
                "-profile:v","main",
                "-preset","veryfast",
                "-b:v", p.vBitrate(),
                "-maxrate", p.vMaxrate(),
                "-bufsize", p.vBufsize(),
                "-c:a","aac",
                "-b:a", p.aBitrate(),
                "-ac","2","-ar","48000",
                "-f","hls",
                "-hls_time","4",
                "-hls_playlist_type","vod",
                "-hls_segment_filename", seg,
                out
        );

        exec(cmd, Duration.ofMinutes(10));
    }

    private void exec(List<String> cmd, Duration timeout) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            StringBuilder log = new StringBuilder();
            try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = br.readLine()) != null) log.append(line).append('\n');
            }
            boolean finished = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) { p.destroyForcibly(); throw new RuntimeException("FFMPEG_TIMEOUT"); }
            int code = p.exitValue();
            if (code != 0) throw new RuntimeException("FFMPEG_FAILED(code="+code+")\n"+tail(log.toString(), 200));
        } catch (Exception e) {
            throw new RuntimeException("FFMPEG_EXEC_ERROR", e);
        }
    }

    private String tail(String s, int lines) {
        var arr = s.split("\n");
        int from = Math.max(0, arr.length - lines);
        return String.join("\n", Arrays.copyOfRange(arr, from, arr.length));
    }
}
