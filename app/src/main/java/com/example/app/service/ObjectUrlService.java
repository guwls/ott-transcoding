package com.example.app.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ObjectUrlService {
    private final MinioClient minio; private final String bucket;
    public ObjectUrlService(MinioClient minio, @Value("${storage.bucket}") String bucket) {
        this.minio=minio; this.bucket=bucket;
    }
    public String presignGet(String objectKey, int minutes) {
        try {
            return minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).bucket(bucket).object(objectKey)
                    .expiry(minutes, TimeUnit.MINUTES).build());
        } catch (Exception e) {
            throw new RuntimeException("PRESIGN_GET_FAILED:"+objectKey, e);
        }
    }
}
