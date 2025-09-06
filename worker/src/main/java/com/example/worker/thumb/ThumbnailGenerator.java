package com.example.worker.thumb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ThumbnailGenerator {

    /** 지정 초(tSec)에서 1장 추출, jpeg 85 품질, 긴 변 1280 이하 */
    //outDir 생성 (없으면 만듦)
    //출력 파일명: thumb_000030.jpg 같은 형식(예: tSec=30이면 30초 지점)
    //ffmpeg를 외부 프로세스로 실행해 한 프레임을 JPG로 추출
    //최대 60초 대기 후 종료 코드 확인(0이 아니면 실패)
    public Path captureAt(Path input, Path outDir, int tSec, int maxWidth) throws IOException {
        try { Files.createDirectories(outDir); } catch (Exception e) { throw new RuntimeException("PREPARE_OUTDIR_FAILED", e); }
        String name = String.format("thumb_%06d.jpg", tSec);
        Path out = outDir.resolve(name);

        var vf = "scale='if(gt(a,1),(min(" + maxWidth + ",iw)),-2)':'if(le(a,1),(min(" + maxWidth + ",ih)),-2)'";
        Process p = new ProcessBuilder(
                "ffmpeg","-y", //존재하면 덮어쓰기
                "-ss", String.valueOf(tSec), "-i", input.toAbsolutePath().toString(), //tSec로 '입력 전' 탐색(빠름, 근사치)
                "-frames:v","1", //정확히 1프레임만 출력
                "-vf", vf, //스케일 필터(긴 변 maxWidth 이하)
                "-q:v","2", //JPEG 품질(값이 낮을수록 고화질, 2~31)
                out.toAbsolutePath().toString()
        ).redirectErrorStream(true).start();

        try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            while (br.readLine() != null) {}
        } catch (Exception ignore) {}

        try {
            boolean finished = p.waitFor(60_000, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished || p.exitValue() != 0) throw new RuntimeException("THUMB_FFMPEG_FAILED");
        } catch (InterruptedException e) {
            throw new RuntimeException("THUMB_FFMPEG_INTERRUPTED", e);
        }
        return out;
    }
}