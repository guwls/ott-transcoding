package com.example.worker.messaging;

import com.example.worker.service.JobStatusService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TranscodeJobListener {

    private static final Logger log = LoggerFactory.getLogger(TranscodeJobListener.class);

    private final JobStatusService statusService;
    private final long simulateMs;

    public TranscodeJobListener(JobStatusService statusService,
                                @Value("${app.worker.simulate-ms:500}") long simulateMs) {
        this.statusService = statusService;
        this.simulateMs = simulateMs;
    }

    @KafkaListener(topics = "${app.topics.transcode-requests}",
            containerFactory = "transcodeListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, TranscodeRequestedEvent> rec, Acknowledgment ack) {
        var key = rec.key();
        var ev = rec.value();
        log.info("[WORKER] recv key={} videoId={} variants={}", key, ev.videoId(), ev.variants());

        try {
            // 상태: RUNNING
            statusService.markRunning(key);

            // 더미 처리 (FFmpeg 대신 sleep)
            Thread.sleep(simulateMs);

            // 상태: SUCCESS
            statusService.markSuccess(key);
            log.info("[WORKER] success key={} jobId={} target={}", key, ev.jobId(), ev.targetPrefix());

            // ✅ 커밋 (성공시에만)
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("[WORKER] failed key={}, err={}", key, ex.toString());
            // 커밋하지 않음 → 재소비(리트라이)로 흘러감
            // 필요 시 DLQ/백오프는 Day 5에서 추가
        }
    }
}