package com.example.app.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class UploadPostService {
    private final MinioClient minio; private final String bucket;
    private final String publicBaseUrl;

    public UploadPostService(MinioClient minio, @Value("${storage.bucket}") String bucket,
                             @Value("${storage.publicBaseUrl}") String publicBaseUrl) {
        this.minio = minio; this.bucket = bucket; this.publicBaseUrl=publicBaseUrl;
    }

    /** 제한: Content-Type video/*, 크기 5MB ~ 2GB, 만료 15분 */
    public record PostForm(String url, Map<String,String> formFields, String objectKey, long expiresSeconds) {}

    public PostForm createPost(String objectKey) {
        try {
            PostPolicy policy = new PostPolicy(bucket, ZonedDateTime.now().plusMinutes(15));
            policy.addEqualsCondition("key", objectKey);
            policy.addStartsWithCondition("Content-Type", "video/");
            policy.addContentLengthRangeCondition(5L * 1024 * 1024, 2L * 1024 * 1024 * 1024);

            Map<String,String> fields = minio.getPresignedPostFormData(policy);
            String url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectKey).method(Method.PUT).expiry(60*15).build());
            // ↑ 일부 클라이언트는 form URL로 엔드포인트/버킷 주소가 필요: https://<endpoint>/<bucket>
            // MinIO Java SDK에는 별도 헬퍼가 없어도 endpoint는 config에서 알 수 있음.
            String postUrl = publicBaseUrl + "/" + bucket;

            return new PostForm(postUrl, fields, objectKey, 15*60);
        } catch (Exception e) {
            throw new RuntimeException("PRESIGNED_POST_FAILED:"+objectKey, e);
        }
    }
}