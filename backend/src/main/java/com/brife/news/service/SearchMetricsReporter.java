package com.brife.news.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchMetricsReporter {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    @Scheduled(
            fixedDelayString = "${news.search.metrics.log-interval-ms:600000}",
            initialDelayString = "${news.search.metrics.log-initial-delay-ms:600000}")
    public void logSnapshot() {
        Snapshot snapshot = snapshot();
        if (snapshot == null || snapshot.searchCount() == 0) {
            return;
        }

        log.info("[SearchMetrics] searches={}, avgTotalMs={}, avgEmbeddingLookupMs={}, " +
                        "embeddingApiCalls={}, avgEmbeddingApiMs={}, avgHybridDbMs={}, fallbacks={}, " +
                        "cacheHits={}, cacheMisses={}, cacheHitRate={}, cacheEvictions={}, cacheSize={}",
                snapshot.searchCount(), format(snapshot.avgTotalMs()), format(snapshot.avgEmbeddingLookupMs()),
                snapshot.embeddingApiCalls(), format(snapshot.avgEmbeddingApiMs()),
                format(snapshot.avgHybridDatabaseMs()), snapshot.fallbackCount(),
                snapshot.cacheHits(), snapshot.cacheMisses(), format(snapshot.cacheHitRate()),
                snapshot.cacheEvictions(), snapshot.cacheSize());
    }

    Snapshot snapshot() {
        Cache springCache = cacheManager.getCache("queryEmbeddings");
        if (!(springCache instanceof CaffeineCache caffeineCache)) {
            return null;
        }

        CacheStats cacheStats = caffeineCache.getNativeCache().stats();
        Timer total = meterRegistry.find("brife.search.total.duration").timer();
        Timer embeddingLookup = meterRegistry.find("brife.search.embedding.lookup.duration").timer();
        Timer embeddingApi = meterRegistry.find("brife.search.embedding.api.duration").timer();
        Timer hybridDatabase = meterRegistry.find("brife.search.database.hybrid.duration").timer();
        Counter fallback = meterRegistry.find("brife.search.fallback.count").counter();
        if (total == null || embeddingLookup == null || embeddingApi == null
                || hybridDatabase == null || fallback == null) {
            return null;
        }

        return new Snapshot(
                total.count(), total.mean(TimeUnit.MILLISECONDS),
                embeddingLookup.mean(TimeUnit.MILLISECONDS),
                embeddingApi.count(), embeddingApi.mean(TimeUnit.MILLISECONDS),
                hybridDatabase.mean(TimeUnit.MILLISECONDS), (long) fallback.count(),
                cacheStats.hitCount(), cacheStats.missCount(), cacheStats.hitRate(),
                cacheStats.evictionCount(), caffeineCache.getNativeCache().estimatedSize());
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    record Snapshot(
            long searchCount,
            double avgTotalMs,
            double avgEmbeddingLookupMs,
            long embeddingApiCalls,
            double avgEmbeddingApiMs,
            double avgHybridDatabaseMs,
            long fallbackCount,
            long cacheHits,
            long cacheMisses,
            double cacheHitRate,
            long cacheEvictions,
            long cacheSize
    ) {
    }
}
