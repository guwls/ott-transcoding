package com.example.worker.service;

import java.time.*;

public class RetryPolicy {
    // 기본값: 15s, 2배씩 증가, 최대 10분, +지터(±10%)
    public static Duration nextDelay(int attempt) {
        long baseMs = 15_000L;
        long maxMs  = 10 * 60_000L;
        double pow  = Math.pow(2.0, Math.max(0, attempt - 1));
        long raw    = (long)Math.min(baseMs * pow, maxMs);
        long jitter = (long)(raw * 0.1);
        return Duration.ofMillis(raw + (long)(Math.random()*jitter) - jitter/2);
    }
}