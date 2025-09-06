package com.example.worker.messaging;

import com.example.worker.service.HlsMultiPipeline;
import com.example.worker.service.JobStatusService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import com.example.worker.service.ThumbnailPipeline;

@Component
public class TranscodeJobListener {

    private static final Logger log = LoggerFactory.getLogger(TranscodeJobListener.class);

    private final JobStatusService status;
    private final HlsMultiPipeline hls;
    private final ThumbnailPipeline thumbs;

    public TranscodeJobListener(JobStatusService status, HlsMultiPipeline hls, ThumbnailPipeline thumbs) {
        this.status = status; this.hls = hls; this.thumbs = thumbs;
    }

    @RetryableTopic( // 👇 재시도/백오프/최대횟수/옵션 DLT
            attempts = "${app.worker.retry.attempts:6}",
            backoff = @Backoff(
                    delay = 15000,            // 15s
                    multiplier = 2.0,         // 지수 증가
                    maxDelay = 600000         // 10분
            ),
            autoCreateTopics = "true",
            dltTopicSuffix = "-dlt",
            include = { RuntimeException.class } // 처리 실패는 런타임 예외로
    )
    @KafkaListener(
            topics = "${app.topics.transcode-requests}",
            containerFactory = "transcodeListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, TranscodeRequestedEvent> rec) {
        String key = rec.key();
        var ev = rec.value();
        var dec = status.beginIfDue(key);

        switch (dec) {
            case ALREADY_SUCCESS -> {
                log.info("[WORKER] skip(SUCCESS) key={} videoId={}", key, ev.videoId());
                return;
            }
            case ALREADY_RUNNING -> {
                log.info("[WORKER] skip(RUNNING) key={} videoId={}", key, ev.videoId());
                return;
            }
            case NOT_DUE -> {
                log.info("[WORKER] skip(NOT_DUE) key={} nextRetryAt>now", key);
                return;
            }
            case MAX_RETRY_EXCEEDED -> {
                log.error("[WORKER] skip(MAX_RETRY_EXCEEDED) key={} attempts limit", key);
                return;
            }
            case NOT_FOUND -> {
                log.error("[WORKER] skip(NOT_FOUND) key={}", key);
                return;
            }
            case PROCEED -> { /* fall-through to actual work */ }
        }

        try {
            // 실제 작업 (멀티 변형 + 썸네일)
            hls.run(ev.videoId(), ev.targetPrefix(), ev.variants());
            try { thumbs.run(ev.videoId()); } catch (Exception te) {
                // 썸네일 실패는 경고만 (정책화 가능)
                log.warn("[THUMBS] failed videoId={} err={}", ev.videoId(), te.toString());
            }
            status.markSuccess(key);
            log.info("[WORKER] success key={} jobId={}", key, ev.jobId());

        } catch (Exception ex) {
            // 실패 기록 + next_retry_at 계산 (DB)
            status.markFailed(key, ex);
            // 예외를 계속 던져서 @RetryableTopic이 재시도/백오프/최종 DLT로 보냄
            throw (ex instanceof RuntimeException re) ? re : new RuntimeException(ex);
        }
    }

    @DltHandler // DLT 최종 도착 시 로그/알림 훅
    public void dlt(ConsumerRecord<String, TranscodeRequestedEvent> rec) {
        log.error("[WORKER][DLT] key={} value={}", rec.key(), rec.value());
        // 필요하면 알림/이벤트 생성 등
    }
}