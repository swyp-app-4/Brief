package com.brife.news.batch;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BatchMetadataHolderTest {

    private final BatchMetadataHolder holder = new BatchMetadataHolder();

    @Test
    void tracksRunningAndSuccessfulCompletionState() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 20, 9, 30);
        LocalDateTime completedAt = LocalDateTime.of(2026, 8, 20, 10, 40);

        holder.updateStartedAt(startedAt);

        assertThat(holder.isBatchRunning()).isTrue();
        assertThat(holder.getLastBatchStartedAt()).contains(startedAt);

        holder.updateCompletedAt(completedAt);
        holder.markFinished();

        assertThat(holder.isBatchRunning()).isFalse();
        assertThat(holder.getLastBatchCompletedAt()).contains(completedAt);
    }

    @Test
    void failedBatchCanFinishWithoutReplacingLastSuccessfulCompletion() {
        LocalDateTime completedAt = LocalDateTime.of(2026, 8, 20, 6, 40);
        holder.updateCompletedAt(completedAt);
        holder.updateStartedAt(LocalDateTime.of(2026, 8, 20, 9, 30));

        holder.markFinished();

        assertThat(holder.isBatchRunning()).isFalse();
        assertThat(holder.getLastBatchCompletedAt()).contains(completedAt);
    }
}
