package com.example.worker.messaging;

import com.example.worker.service.Hls720Pipeline;
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
    private final Hls720Pipeline pipeline;
    private final boolean enable720 = true;

    public TranscodeJobListener(JobStatusService statusService, Hls720Pipeline pipeline,
                                @Value("${app.worker.simulate-ms:0}") long simulateMs) {
        this.statusService = statusService;
        this.pipeline = pipeline;
    }

    @KafkaListener(topics = "${app.topics.transcode-requests}",
            containerFactory = "transcodeListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, TranscodeRequestedEvent> rec, Acknowledgment ack) {
        var key = rec.key();
        var ev = rec.value();
        log.info("[WORKER] recv key={} videoId={} variants={}", key, ev.videoId(), ev.variants());
        try {
            statusService.markRunning(key);

            // 오늘은 720p만 처리
            if (enable720 && ev.variants() != null && ev.variants().stream().anyMatch(v -> v.equalsIgnoreCase("720p"))) {
                pipeline.run(ev.videoId(), ev.targetPrefix());
            }

            statusService.markSuccess(key);
            log.info("[WORKER] success key={} jobId={} target={}", key, ev.jobId(), ev.targetPrefix());
            ack.acknowledge(); //공시에만 커밋

        } catch (Exception ex) {
            // 표준화된 에러 코드가 포함되도록 메시지 작성 (Storage/FFMPEG 런타임에서 넣었음)
            log.error("[WORKER] FAILED key={} videoId={} code? err={}", key, ev.videoId(), ex.toString());
            //미커밋 → 재소비
        }
    }
}