package com.example.worker.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transcode_job")
public class TranscodeJob {

    public enum Status { QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="job_key", nullable=false, length=50, unique=true)
    private String jobKey;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private Status status;

    @Column(name="attempt_count", nullable=false) private int attemptCount;
    @Column(name="last_error") private String lastError;
    @Column(name="next_retry_at") private LocalDateTime nextRetryAt;

    @Version @Column(name="version", nullable=false)
    private int version;

    @Column(name="updated_at", insertable=false, updatable=false) private LocalDateTime updatedAt;

    public TranscodeJob() {}

    public Long getId(){ return id; }
    public String getJobKey(){ return jobKey; }
    public Status getStatus(){ return status; }
    public void setStatus(Status s){ this.status = s; }
    public int getAttemptCount(){ return attemptCount; }
    public void incAttempt(){ this.attemptCount++; }
    public String getLastError(){ return lastError; }
    public void setLastError(String e){ this.lastError = e; }
    public LocalDateTime getNextRetryAt(){ return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime t){ this.nextRetryAt = t; }
}
