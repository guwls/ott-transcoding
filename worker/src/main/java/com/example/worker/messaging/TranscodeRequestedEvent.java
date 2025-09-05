package com.example.worker.messaging;

import java.time.OffsetDateTime;
import java.util.List;

public record TranscodeRequestedEvent(
        Long jobId, Long videoId, String jobKey, String targetPrefix,
        List<String> variants, OffsetDateTime requestedAt
) { }

