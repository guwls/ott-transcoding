package com.example.worker.domain;

import jakarta.persistence.*;

@Entity @Table(name="video")
public class Video {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="original_key", nullable=false, length=300, unique=true)
    private String originalKey;

    protected Video() {}
    public Long getId(){ return id; }
    public String getOriginalKey(){ return originalKey; }
}
