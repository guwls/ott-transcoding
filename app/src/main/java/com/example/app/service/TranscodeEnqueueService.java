package com.example.app.service;

import com.example.app.domain.TranscodeJob;
import com.example.app.repo.TranscodeJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TranscodeEnqueueService {

    private final TranscodeJobRepository jobRepo;
    private final ObjectMapper om;

    public TranscodeEnqueueService(TranscodeJobRepository jobRepo, ObjectMapper om) {
        this.jobRepo = jobRepo;
        this.om = om;
    }

    /** 클라이언트가 보낸 Idempotency-Key가 있으면 사용, 없으면 새 UUID 생성 */
    private String resolveJobKey(String idempotencyKeyHeader) {
        String key = (idempotencyKeyHeader == null || idempotencyKeyHeader.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKeyHeader.trim();
        // DB 스키마가 VARCHAR(50) 이므로 방어적 클램프
        if (key.length() > 50) key = key.substring(0, 50);
        return key;
    }

    public record EnqueueResult(Long jobId, String jobKey, String status, boolean created) {}

    @Transactional
    public EnqueueResult enqueue(Long videoId,
                                 List<String> variants,
                                 String targetPrefix,
                                 String idempotencyKeyHeader) {
        String jobKey = resolveJobKey(idempotencyKeyHeader);

        // 1) 같은 jobKey로 이미 생성된 작업이 있으면 그대로 반환(멱등)
        var existing = jobRepo.findByJobKey(jobKey);
        if (existing.isPresent()) {
            var j = existing.get();
            return new EnqueueResult(j.getId(), j.getJobKey(), j.getStatus().name(), false);
        }

        // 2) 없으면 새로 저장
        String variantsJson;
        try {
            variantsJson = om.writeValueAsString(variants);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("variants 직렬화 실패", e);
        }

        TranscodeJob j = jobRepo.save(
                TranscodeJob.queued(videoId, jobKey, targetPrefix, variantsJson)
        );
        return new EnqueueResult(j.getId(), j.getJobKey(), j.getStatus().name(), true);
    }
}
