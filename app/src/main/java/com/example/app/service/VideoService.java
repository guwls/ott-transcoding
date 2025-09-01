package com.example.app.service;

import com.example.app.domain.Video;
import com.example.app.repo.VideoRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final MinioClient minio;
    @Value("${storage.bucket}") private String bucket;

    public VideoService(VideoRepository videoRepository, MinioClient minio) {
        this.videoRepository = videoRepository; this.minio = minio;
    }

    public record CreateVideoRequest(
            @NotBlank @jakarta.validation.constraints.Size(max=200) String title,
            @PositiveOrZero long filesize
    ) {}

    public record CreateVideoResponse(Long videoId, String uploadUrl, String objectKey) {}


    @Transactional
    public CreateVideoResponse createAndIssueUploadUrl(CreateVideoRequest req, Long uploaderId) {
        // 1) 서버-생성 원본 키 (덮어쓰기·보안 이슈 방지)
        String objectKey = "uploads/%s.mp4".formatted(UUID.randomUUID());
        Video v = videoRepository.save(Video.create(uploaderId, req.title(), objectKey));

        // 2) Presigned PUT (10분)
        try {
            String url = minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT).bucket(bucket).object(objectKey)
                            .expiry(60 * 10)
                            .build()
            );
            return new CreateVideoResponse(v.getId(), url, objectKey);
        } catch (Exception e) {
            // 예외 → 트랜잭션 롤백(고아 레코드 없음)
            throw new IllegalStateException("Presigned URL 발급 실패", e);
        }
    }
}
