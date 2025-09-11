package com.example.app.ratelimit;

import org.springframework.beans.factory.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TokenBucketRateLimiter {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<List> script;
    private final int capacity;
    private final int refillPerSec;

    public static record Verdict(boolean allowed, int remaining, long retryAfterMs) {}

    public TokenBucketRateLimiter(StringRedisTemplate redis,
                                  DefaultRedisScript<List> script,
                                  @Value("${rate-limit.enqueue.capacity}") int capacity,
                                  @Value("${rate-limit.enqueue.refillPerSec}") int refillPerSec) {
        this.redis = redis; this.script = script;
        this.capacity = capacity; this.refillPerSec = refillPerSec;
    }

    public Verdict allow(String bucketKey) {
        long now = Instant.now().toEpochMilli();
        @SuppressWarnings("unchecked")
        List<Long> res = (List<Long>) redis.execute(script, List.of(bucketKey),
                String.valueOf(capacity), String.valueOf(refillPerSec), String.valueOf(now));
        boolean allowed = res.get(0) == 1L;
        int remaining = res.get(1).intValue();
        long retryMs = res.get(2);
        return new Verdict(allowed, remaining, retryMs);
    }

    public int capacity()     { return capacity; }
    public int refillPerSec() { return refillPerSec; }
}