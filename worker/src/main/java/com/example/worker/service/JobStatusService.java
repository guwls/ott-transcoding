package com.example.worker.service;

import com.example.worker.domain.TranscodeJob;
import com.example.worker.repo.TranscodeJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
public class JobStatusService {

    public enum Decision { PROCEED, ALREADY_SUCCESS, ALREADY_RUNNING, NOT_DUE, MAX_RETRY_EXCEEDED, NOT_FOUND }

    private final TranscodeJobRepository repo;
    private final int maxAttempts;

    public JobStatusService(TranscodeJobRepository repo,
                            @org.springframework.beans.factory.annotation.Value("${app.worker.max-attempts:6}") int maxAttempts) {
        this.repo = repo; this.maxAttempts = Math.max(1, maxAttempts);
    }

    /** 멱등/스케줄 가드: 처리해도 되는지 판단하고 RUNNING+attempt++ 까지 반영 */
    @Transactional
    public Decision beginIfDue(String jobKey) {
        var now = LocalDateTime.now();
        var opt = repo.lockByJobKey(jobKey);
        if (opt.isEmpty()) return Decision.NOT_FOUND;

        var j = opt.get();

        if (j.getStatus() == TranscodeJob.Status.SUCCESS) return Decision.ALREADY_SUCCESS;
        if (j.getStatus() == TranscodeJob.Status.RUNNING) return Decision.ALREADY_RUNNING;

        if (j.getAttemptCount() >= maxAttempts) return Decision.MAX_RETRY_EXCEEDED;
        if (j.getNextRetryAt() != null && j.getNextRetryAt().isAfter(now)) return Decision.NOT_DUE;

        // 진행 허용
        j.setStatus(TranscodeJob.Status.RUNNING);
        j.setLastError(null);
        j.incAttempt(); // 이번 시도 반영
        return Decision.PROCEED;
    }

    @Transactional
    public void markSuccess(String jobKey) {
        var j = repo.lockByJobKey(jobKey).orElseThrow();
        j.setStatus(TranscodeJob.Status.SUCCESS);
        j.setLastError(null);
        j.setNextRetryAt(null);
    }

    @Transactional
    public void markFailed(String jobKey, Throwable ex) {
        var j = repo.lockByJobKey(jobKey).orElseThrow();
        j.setStatus(TranscodeJob.Status.FAILED);
        j.setLastError(shortMsg(ex));
        var delay = RetryPolicy.nextDelay(j.getAttemptCount());
        j.setNextRetryAt(LocalDateTime.now().plus(delay));
    }

    private String shortMsg(Throwable ex) {
        var m = ex == null ? "" : ex.getMessage();
        if (m == null) m = ex.toString();
        return (m.length() > 300) ? m.substring(0,300) : m;
    }
}
