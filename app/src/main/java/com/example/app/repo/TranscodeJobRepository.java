package com.example.app.repo;

import com.example.app.domain.TranscodeJob;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TranscodeJobRepository extends JpaRepository<TranscodeJob, Long> {

    Optional<TranscodeJob> findByJobKey(String jobKey);

    boolean existsByJobKey(String jobKey);

    // 워커에서 안전하게 한 건을 집어 처리하고 싶을 때
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from TranscodeJob j where j.jobKey = :jobKey")
    Optional<TranscodeJob> findByJobKeyForUpdate(@Param("jobKey") String jobKey);
}
