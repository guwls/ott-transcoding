package com.example.app.service;

import com.example.app.domain.Video;
import com.example.app.repo.VideoRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock MinioClient minio;
    @Mock VideoRepository videoRepository;
    @InjectMocks VideoService videoService;

    @Test
    void createAndIssueUploadUrl_ok() throws Exception {
        ReflectionTestUtils.setField(videoService, "bucket", "media");

        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> {
            Video v = inv.getArgument(0);
            var f = Video.class.getDeclaredField("id"); f.setAccessible(true); f.set(v, 123L);
            return v;
        });
        when(minio.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/media/uploads/test.mp4?sig=abc");

        var res = videoService.createAndIssueUploadUrl(
                new VideoService.CreateVideoRequest("title", 1000), 1L);

        assertThat(res.videoId()).isEqualTo(123L);
        assertThat(res.objectKey()).startsWith("uploads/");
        assertThat(res.uploadUrl()).contains("http://localhost:9000");
        verify(videoRepository).save(any(Video.class));
        verify(minio).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void createAndIssueUploadUrl_minioFails_throws() throws Exception {
        ReflectionTestUtils.setField(videoService, "bucket", "media");
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));
        when(minio.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("minio down"));

        try {
            videoService.createAndIssueUploadUrl(new VideoService.CreateVideoRequest("title", 1000), 1L);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("Presigned URL 발급 실패");
        }
    }
}