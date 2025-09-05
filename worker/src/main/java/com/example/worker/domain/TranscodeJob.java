package com.example.worker.domain;

import jakarta.persistence.*;

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

    @Version @Column(name="version", nullable=false)
    private int version;

    public TranscodeJob() {}

    public Long getId() { return id; }
    public String getJobKey() { return jobKey; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
}
