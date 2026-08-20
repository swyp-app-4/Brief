package com.brife.news.batch;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class BatchMetadataHolder {

    private volatile LocalDateTime lastBatchStartedAt;
    private volatile LocalDateTime lastBatchCompletedAt;
    private volatile boolean batchRunning;

    public void updateStartedAt(LocalDateTime time) {
        this.lastBatchStartedAt = time;
        this.batchRunning = true;
    }

    public void updateCompletedAt(LocalDateTime time) {
        this.lastBatchCompletedAt = time;
    }

    public void markFinished() {
        this.batchRunning = false;
    }

    public Optional<LocalDateTime> getLastBatchStartedAt() {
        return Optional.ofNullable(lastBatchStartedAt);
    }

    public Optional<LocalDateTime> getLastBatchCompletedAt() {
        return Optional.ofNullable(lastBatchCompletedAt);
    }

    public boolean isBatchRunning() {
        return batchRunning;
    }
}
