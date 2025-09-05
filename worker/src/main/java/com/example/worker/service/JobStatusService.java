package com.example.worker.service;

import com.example.worker.domain.TranscodeJob;
import com.example.worker.repo.TranscodeJobRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobStatusService {

    private final TranscodeJobRepository repo;
    public JobStatusService(TranscodeJobRepository repo) { this.repo = repo; }

    @Transactional
    public void markRunning(String jobKey) {
        var job = repo.lockByJobKey(jobKey)
                .orElseThrow(() -> new IllegalStateException("job not found: " + jobKey));
        if (job.getStatus() == TranscodeJob.Status.SUCCESS) return; // 이미 끝난 작업은 무시
        job.setStatus(TranscodeJob.Status.RUNNING);
        // flush는 트랜잭션 끝에 자동. 락으로 동시 갱신 충돌 방지.
    }

    @Transactional
    public void markSuccess(String jobKey) {
        var job = repo.lockByJobKey(jobKey)
                .orElseThrow(() -> new IllegalStateException("job not found: " + jobKey));
        job.setStatus(TranscodeJob.Status.SUCCESS);
    }

    @Transactional
    public void markFailed(String jobKey) {
        var job = repo.lockByJobKey(jobKey)
                .orElseThrow(() -> new IllegalStateException("job not found: " + jobKey));
        job.setStatus(TranscodeJob.Status.FAILED);
    }
}
