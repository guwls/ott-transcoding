package com.example.worker.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class StorageConfig {
    @Bean
    public MinioClient minioClient(
            @Value("${storage.endpoint}") String endpoint,
            @Value("${storage.accessKey}") String accessKey,
            @Value("${storage.secretKey}") String secretKey) {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }
}