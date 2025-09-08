package com.example.app.controller;

import com.example.app.service.UploadPostService;
import com.example.app.service.ObjectKeyPolicy;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@RestController
@RequestMapping("/videos/upload")
public class VideoUploadController {

    private final UploadPostService posts;
    private final ObjectKeyPolicy keyPolicy;

    public VideoUploadController(UploadPostService posts, ObjectKeyPolicy keyPolicy) {
        this.posts = posts; this.keyPolicy = keyPolicy;
    }

    public record CreateVideoRequest(@NotBlank String title, long filesize) {}
    public record UploadInfo(String method, String url, Map<String,String> formFields, String objectKey, long expiresSeconds) {}
    public record CreateVideoResponse(Long videoId, UploadInfo upload) {}

    @PostMapping
    public CreateVideoResponse createUploadForm(@RequestBody CreateVideoRequest req) {
        // ... video 엔티티 생성/저장 (생략, 기존 로직)
        Long videoId = null; /* saved id */;
        String objectKey = keyPolicy.uploadKeyFor(videoId, "mp4"); // 아래 4) 참조

        var form = posts.createPost(objectKey);
        return new CreateVideoResponse(videoId,
                new UploadInfo("POST", form.url(), form.formFields(), form.objectKey(), form.expiresSeconds()));
    }
}
