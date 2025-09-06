package com.example.app.repo;

import com.example.app.domain.Thumbnail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {
    List<Thumbnail> findByVideoIdOrderByTimeSecAsc(Long videoId);
}
