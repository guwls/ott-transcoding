package com.example.app.messaging;

import org.springframework.kafka.support.SendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
    public CompletableFuture<SendResult<String,TranscodeRequestedEvent>>
    publish(Long jobId, Long videoId, String jobKey, String targetPrefix, List<String> variants) {
        var event = new TranscodeRequestedEvent(jobId, videoId, jobKey, targetPrefix, variants, OffsetDateTime.now());
        return kt.send(topic, jobKey, event);
    }
}
