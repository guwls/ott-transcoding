package com.example.worker.ffmpeg;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FfprobeUtil {
    /** 초 단위 길이(소수) 반환 */
    public static double durationSeconds(Path inputMp4) {
        try {
            Process p = new ProcessBuilder(
                    "ffprobe","-v","error", //에러만 출력
                    "-show_entries","format=duration", //컨테이너 포맷 레벨의 duration만 요구
                    "-of","default=noprint_wrappers=1:nokey=1", //래퍼 제거, 키 이름 제거 -> 숫자만 한 줄로 나오게 함
                    inputMp4.toAbsolutePath().toString()
            ).redirectErrorStream(true).start(); //stderr를 stdout에 합쳐 표준 스트림 하나만 읽음
            String out;
            //BufferedReader로 첫 줄을 읽고
            try (var br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                out = br.readLine();
            }
            p.waitFor();
            //Double.parseDouble로 변환해서 반환
            return Double.parseDouble(out);
        } catch (Exception e) {
            throw new RuntimeException("FFPROBE_FAILED", e);
        }
    }
}
