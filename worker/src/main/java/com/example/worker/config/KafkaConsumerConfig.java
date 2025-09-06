package com.example.worker.config;

import com.example.worker.messaging.TranscodeRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, TranscodeRequestedEvent> transcodeConsumerFactory(KafkaProperties props) {
        Map<String, Object> cfg = props.buildConsumerProperties(null);
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // JsonDeserializer는 default.type을 application.yml에서 받음
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TranscodeRequestedEvent>
    transcodeListenerContainerFactory(ConsumerFactory<String, TranscodeRequestedEvent> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, TranscodeRequestedEvent>();
        f.setConsumerFactory(cf);
        f.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);
        f.setConcurrency(2); // 워커 병렬성
        return f;
    }
}
