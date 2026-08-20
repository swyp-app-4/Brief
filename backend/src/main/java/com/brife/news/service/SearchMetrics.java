package com.brife.news.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SearchMetrics {

    private final Timer totalTimer;
    private final Timer embeddingLookupTimer;
    private final Timer embeddingApiTimer;
    private final Timer hybridDatabaseTimer;
    private final Timer fallbackDatabaseTimer;
    private final Counter fallbackCounter;

    public SearchMetrics(MeterRegistry meterRegistry) {
        this.totalTimer = meterRegistry.timer("brife.search.total.duration");
        this.embeddingLookupTimer = meterRegistry.timer("brife.search.embedding.lookup.duration");
        this.embeddingApiTimer = meterRegistry.timer("brife.search.embedding.api.duration");
        this.hybridDatabaseTimer = meterRegistry.timer("brife.search.database.hybrid.duration");
        this.fallbackDatabaseTimer = meterRegistry.timer("brife.search.database.fallback.duration");
        this.fallbackCounter = meterRegistry.counter("brife.search.fallback.count");
    }

    public void recordTotal(long startedAtNanos) {
        record(totalTimer, startedAtNanos);
    }

    public void recordEmbeddingLookup(long startedAtNanos) {
        record(embeddingLookupTimer, startedAtNanos);
    }

    public void recordEmbeddingApi(long startedAtNanos) {
        record(embeddingApiTimer, startedAtNanos);
    }

    public void recordHybridDatabase(long startedAtNanos) {
        record(hybridDatabaseTimer, startedAtNanos);
    }

    public void recordFallbackDatabase(long startedAtNanos) {
        record(fallbackDatabaseTimer, startedAtNanos);
    }

    public void incrementFallback() {
        fallbackCounter.increment();
    }

    private void record(Timer timer, long startedAtNanos) {
        timer.record(System.nanoTime() - startedAtNanos, TimeUnit.NANOSECONDS);
    }
}
