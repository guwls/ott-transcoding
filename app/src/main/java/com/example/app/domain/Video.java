package com.example.app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video")
public class Video {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long uploaderId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 300, unique = true)
    private String originalKey;

    private Integer durationSec;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.UPLOADED;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Video() {} // JPA 기본 생성자

    public static Video create(Long uploaderId, String title, String originalKey) {
        Video v = new Video();
        v.uploaderId = uploaderId;
        v.title = title;
        v.originalKey = originalKey;
        return v;
    }

    public enum Status { UPLOADED, QUEUED, PROCESSING, READY, FAILED }

    // 최소 getter
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getOriginalKey() { return originalKey; }
    public Status getStatus() { return status; }
}
