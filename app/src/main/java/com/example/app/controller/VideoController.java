package com.example.app.controller;

import com.example.app.service.VideoService;
import com.example.app.service.VideoService.CreateVideoRequest;
import com.example.app.service.VideoService.CreateVideoResponse;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/videos")
public class VideoController {
    private final VideoService videoService;
    public VideoController(VideoService videoService) { this.videoService = videoService; }

    @io.swagger.v3.oas.annotations.tags.Tag(name = "Video")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "업로드 등록", description = "Video를 생성하고 Presigned PUT URL을 발급합니다."
    )
    @PostMapping
    public CreateVideoResponse create(@RequestBody CreateVideoRequest req) {
        return videoService.createAndIssueUploadUrl(req, 1L); // uploaderId 임시
    }
}
