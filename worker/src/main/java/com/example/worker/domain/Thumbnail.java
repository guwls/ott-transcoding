package com.example.worker.domain;

import jakarta.persistence.*;

@Entity @Table(name="thumbnail")
public class Thumbnail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="video_id", nullable=false) private Long videoId;
    @Column(name="object_key", nullable=false, length=400) private String objectKey;
    @Column(name="time_sec", nullable=false) private int timeSec;
    @Column(name="width") private Integer width;
    @Column(name="height") private Integer height;

    protected Thumbnail() {}
    public Thumbnail(Long videoId, String key, int sec, Integer w, Integer h) {
        this.videoId=videoId; this.objectKey=key; this.timeSec=sec; this.width=w; this.height=h;
    }
}