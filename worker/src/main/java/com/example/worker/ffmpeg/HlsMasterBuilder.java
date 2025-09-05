package com.example.worker.ffmpeg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class HlsMasterBuilder {

    public static void writeMaster(Path masterPath, List<HlsPreset> variants) {
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n"); //M3U 시작 선언
        sb.append("#EXT-X-VERSION:3\n"); //HLS버전

        for (HlsPreset p : variants) {
            long bw = parseRateToBps(p.vMaxrate()) + parseRateToBps(p.aBitrate()); // 대략치
            sb.append("#EXT-X-STREAM-INF:")
                    .append("BANDWIDTH=").append(bw).append(',') //최대 비트레이트
                    .append("RESOLUTION=").append(p.width()).append('x').append(p.height()).append(',')
                    .append("CODECS=\"avc1.42e01e,mp4a.40.2\"") //비디오 H.264, 오디오는 AAC-LC
                    .append("\n")
                    .append(p.name()).append("/index.m3u8\n"); //변형 상대 경로
        }

        try {
            //파일로 쓰기 -> 없으면 만들고, 있으면 덮어씀
            Files.writeString(masterPath, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("MASTER_WRITE_FAILED:"+masterPath, e);
        }
    }

    /** "2800k", "5M", "128k" → bps 근사치 */
    private static long parseRateToBps(String s) {
        String t = s.trim().toLowerCase();
        try {
            if (t.endsWith("k")) return Long.parseLong(t.substring(0, t.length()-1)) * 1000L;
            if (t.endsWith("m")) return Long.parseLong(t.substring(0, t.length()-1)) * 1000_000L;
            if (t.endsWith("bps")) return Long.parseLong(t.replace("bps","").trim());
            return Long.parseLong(t); // 숫자만
        } catch (NumberFormatException e) {
            return 1000_000L; // 파싱 실패시 안전한 기본값
        }
    }
}
