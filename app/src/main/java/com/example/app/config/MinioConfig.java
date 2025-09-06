package com.example.app.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient(@Value("${storage.endpoint}") String endpoint,
                                   @Value("${storage.accessKey}") String accessKey,
                                   @Value("${storage.secretKey}") String secret) {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secret).build();
    }
}
