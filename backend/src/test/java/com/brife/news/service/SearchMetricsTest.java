package com.brife.news.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchMetricsTest {

    @Test
    void recordsSearchStageTimersAndFallbackCount() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SearchMetrics metrics = new SearchMetrics(registry);
        long startedAt = System.nanoTime() - 1_000_000;

        metrics.recordTotal(startedAt);
        metrics.recordEmbeddingLookup(startedAt);
        metrics.recordEmbeddingApi(startedAt);
        metrics.recordHybridDatabase(startedAt);
        metrics.recordFallbackDatabase(startedAt);
        metrics.incrementFallback();

        assertThat(registry.get("brife.search.total.duration").timer().count()).isEqualTo(1);
        assertThat(registry.get("brife.search.embedding.lookup.duration").timer().count()).isEqualTo(1);
        assertThat(registry.get("brife.search.embedding.api.duration").timer().count()).isEqualTo(1);
        assertThat(registry.get("brife.search.database.hybrid.duration").timer().count()).isEqualTo(1);
        assertThat(registry.get("brife.search.database.fallback.duration").timer().count()).isEqualTo(1);
        assertThat(registry.get("brife.search.fallback.count").counter().count()).isEqualTo(1);
    }
}
