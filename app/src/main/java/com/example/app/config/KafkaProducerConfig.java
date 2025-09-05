package com.example.app.config;

import com.example.app.messaging.TranscodeRequestedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, TranscodeRequestedEvent> transcodeProducerFactory(KafkaProperties props) {
        Map<String,Object> cfg = props.buildProducerProperties(null);
        cfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(cfg);
    }
    @Bean
    public KafkaTemplate<String, TranscodeRequestedEvent> transcodeKafkaTemplate(
            ProducerFactory<String, TranscodeRequestedEvent> pf) {
        return new KafkaTemplate<>(pf);
    }
    @Bean
    public NewTopic transcodeRequestsTopic(KafkaTopicsProperties tp) {
        return TopicBuilder.name(tp.transcodeRequests()).partitions(3).replicas(1).build();
    }
}
@ConfigurationProperties(prefix = "app.topics")
class KafkaTopicsProperties {
    private String transcodeRequests;
    public String transcodeRequests() { return transcodeRequests; }
    public void setTranscodeRequests(String v){ this.transcodeRequests = v; }
}
