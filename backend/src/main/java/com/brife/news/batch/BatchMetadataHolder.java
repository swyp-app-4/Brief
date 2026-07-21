package com.brife.news.batch;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class BatchMetadataHolder {

    private volatile LocalDateTime lastBatchStartedAt;

    public void updateStartedAt(LocalDateTime time) {
        this.lastBatchStartedAt = time;
    }

    public Optional<LocalDateTime> getLastBatchStartedAt() {
        return Optional.ofNullable(lastBatchStartedAt);
    }
}
