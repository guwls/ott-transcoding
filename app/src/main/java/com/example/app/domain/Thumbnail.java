package com.example.app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="thumbnail",
        uniqueConstraints = @UniqueConstraint(name="uk_thumb_key", columnNames="object_key"),
        indexes = @Index(name="ix_thumb_video", columnList="video_id"))
public class Thumbnail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="video_id", nullable=false) private Long videoId;
    @Column(name="object_key", nullable=false, length=400) private String objectKey;
    @Column(name="time_sec", nullable=false) private int timeSec;
    @Column(name="width") private Integer width;
    @Column(name="height") private Integer height;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();

    protected Thumbnail() {}
    public Thumbnail(Long videoId, String key, int sec, Integer w, Integer h) {
        this.videoId=videoId; this.objectKey=key; this.timeSec=sec; this.width=w; this.height=h;
    }

    public Long getId() {
        return id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public int getTimeSec() {
        return timeSec;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
