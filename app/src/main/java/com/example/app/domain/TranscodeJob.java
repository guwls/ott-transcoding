package com.example.app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcode_job",
        uniqueConstraints = @UniqueConstraint(name = "uk_job_key", columnNames = "job_key"),
        indexes = { @Index(name = "ix_video_id", columnList = "video_id") })
public class TranscodeJob {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_key", nullable = false, length = 50)
    private String jobKey;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "target_prefix", nullable = false, length = 255)
    private String targetPrefix; // 예: videos/{videoId}/hls/

    @Column(name = "variants_json", nullable = false, columnDefinition = "JSON")
    private String variantsJson; // ["480p","720p","1080p"]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.QUEUED;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Version
    @Column(name = "version", nullable = false)
    private int version; // 낙관적 락

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected TranscodeJob() {}

    private TranscodeJob(String jobKey, Long videoId, String targetPrefix, String variantsJson) {
        this.jobKey = jobKey;
        this.videoId = videoId;
        this.targetPrefix = targetPrefix;
        this.variantsJson = variantsJson;
        this.status = Status.QUEUED;
    }

    public static TranscodeJob queued(Long videoId, String jobKey, String targetPrefix, String variantsJson) {
        return new TranscodeJob(jobKey, videoId, targetPrefix, variantsJson);
    }

    // 상태 전이 헬퍼 (추후 서비스에서 사용)
    public void markRunning() {
        this.status = Status.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }
    public void markSuccess() {
        this.status = Status.SUCCESS;
        this.updatedAt = LocalDateTime.now();
    }
    public void markFailed(String error, LocalDateTime nextRetryAt) {
        this.status = Status.FAILED;
        this.lastError = error;
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = LocalDateTime.now();
    }
    public void incAttempt() {
        this.attemptCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status { QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED }

    // 최소 getter (서비스/리스너에서 필요해요)
    public Long getId() { return id; }
    public String getJobKey() { return jobKey; }
    public Long getVideoId() { return videoId; }
    public String getTargetPrefix() { return targetPrefix; }
    public String getVariantsJson() { return variantsJson; }
    public Status getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public String getLastError() { return lastError; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
}