package com.example.worker.messaging;

import com.example.worker.service.HlsMultiPipeline;
import com.example.worker.service.JobStatusService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import com.example.worker.service.ThumbnailPipeline;

@Component
public class TranscodeJobListener {

    private static final Logger log = LoggerFactory.getLogger(TranscodeJobListener.class);

    private final JobStatusService statusService;
    private final HlsMultiPipeline pipeline;

    private final ThumbnailPipeline thumbPipeline;
    public TranscodeJobListener(JobStatusService statusService,
                                com.example.worker.service.HlsMultiPipeline pipeline,
                                ThumbnailPipeline thumbPipeline) {
        this.statusService = statusService; this.pipeline = pipeline; this.thumbPipeline = thumbPipeline;
    }

    @KafkaListener(topics = "${app.topics.transcode-requests}",
            containerFactory = "transcodeListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, TranscodeRequestedEvent> rec, Acknowledgment ack) {
        var key = rec.key(); var ev = rec.value();
        log.info("[WORKER] recv key={} videoId={} variants={}", key, ev.videoId(), ev.variants());
        try {
            statusService.markRunning(key);
            pipeline.run(ev.videoId(), ev.targetPrefix(), ev.variants());
            statusService.markSuccess(key);
            ack.acknowledge();
            log.info("[WORKER] success key={} jobId={}", key, ev.jobId());
        } catch (Exception ex) {
            log.error("[WORKER] FAILED key={} err={}", key, ex.toString());
            // 미커밋 → 재소비
        }
    }
}