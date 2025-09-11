package com.example.app.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.kafka.support.SendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class TranscodeProducer {
    private final KafkaTemplate<String, TranscodeRequestedEvent> kt;
    private final String topic;
    public TranscodeProducer(KafkaTemplate<String, TranscodeRequestedEvent> kt,
                             @Value("${app.topics.transcode-requests}") String topic) {
        this.kt = kt; this.topic = topic;
    }

    public CompletableFuture<SendResult<String, TranscodeRequestedEvent>> publish(
            Long jobId, Long videoId, String jobKey, String targetPrefix, List<String> variants) {

        var event = new TranscodeRequestedEvent(
                jobId, videoId, jobKey, targetPrefix, variants, OffsetDateTime.now());

        // 1) 레코드 생성 (topic, key, value)
        var record = new ProducerRecord<String, TranscodeRequestedEvent>(topic, jobKey, event);

        // 2) traceId 헤더 추가 (있을 때만)
        var traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            record.headers().add(new RecordHeader("x-trace-id", traceId.getBytes()));
        }

        // 3) 전송
        return kt.send(record);
    }
}
