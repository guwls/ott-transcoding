package com.example.app.controller;

import com.example.app.service.TranscodeEnqueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary="Enqueue transcode job", description="variants 지정 후 큐 투입")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="enqueued"),
            @ApiResponse(responseCode="401", description="unauthorized",
                    content=@Content(schema=@Schema(implementation=com.example.app.error.ErrorResponse.class))),
            @ApiResponse(responseCode="404", description="video not found",
                    content=@Content(schema=@Schema(implementation=com.example.app.error.ErrorResponse.class))),
            @ApiResponse(responseCode="409", description="idempotency conflict",
                    content=@Content(schema=@Schema(implementation=com.example.app.error.ErrorResponse.class))),
            @ApiResponse(responseCode="429", description="rate limited",
                    content=@Content(schema=@Schema(implementation=com.example.app.error.ErrorResponse.class)))
    })
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
