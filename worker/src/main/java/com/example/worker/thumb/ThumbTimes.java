package com.example.worker.thumb;

import java.util.*;
import java.util.stream.Collectors;

public class ThumbTimes {
    /** durationSec 기준 10s/25%/50%/75% → [초] 리스트 */
    public static List<Integer> pick(double durationSec) {
        List<Double> cand = List.of(
                10.0,
                durationSec * 0.25,
                durationSec * 0.5,
                durationSec * 0.75
        );
        // 0 < t < duration-1, 중복 제거, 오름차순, 정수 초로
        return cand.stream()
                .map(d -> Math.max(1.0, Math.min(d, durationSec - 1.0)))
                .map(Double::intValue)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}

