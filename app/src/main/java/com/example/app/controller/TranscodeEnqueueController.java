package com.example.app.controller;

import com.example.app.service.TranscodeEnqueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transcode")
@RestController
@RequestMapping("/videos/{videoId}/enqueue-transcode")
public class TranscodeEnqueueController {

    private final TranscodeEnqueueService service;

    public TranscodeEnqueueController(TranscodeEnqueueService service) {
        this.service = service;
    }

    public record EnqueueRequest(@NotEmpty List<String> variants) {}
    public record EnqueueResponse(Long jobId, String jobKey, String status, boolean created) {}

    @Operation(summary = "트랜스코딩 작업 큐 투입(멱등)",
            description = "Idempotency-Key 헤더가 같으면 중복 생성 없이 동일 작업을 반환합니다.")
    @PostMapping
    public EnqueueResponse enqueue(@PathVariable Long videoId,
                                   @RequestBody EnqueueRequest req,
                                   @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        // 기본 출력 경로 규칙(예: videos/{videoId}/hls/)
        String targetPrefix = "videos/%d/hls/".formatted(videoId);

        var r = service.enqueue(
                videoId,
                (req.variants() == null || req.variants().isEmpty())
                        ? List.of("480p", "720p", "1080p")
                        : req.variants(),
                targetPrefix,
                idemKey
        );
        return new EnqueueResponse(r.jobId(), r.jobKey(), r.status(), r.created());
    }
}
