package com.example.worker.repo;

import com.example.worker.domain.TranscodeJob;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TranscodeJobRepository extends JpaRepository<TranscodeJob, Long> {

    Optional<TranscodeJob> findByJobKey(String jobKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from TranscodeJob j where j.jobKey = :jobKey")
    Optional<TranscodeJob> lockByJobKey(@Param("jobKey") String jobKey);
}
