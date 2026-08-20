package com.brife.news.service;

import com.brife.news.config.CacheConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class SearchMetricsReporterTest {

    @Test
    void exposesCumulativeSearchAndCacheStatisticsForLogging() {
        SimpleCacheManager cacheManager = (SimpleCacheManager) new CacheConfig().cacheManager();
        cacheManager.afterPropertiesSet();
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        SearchMetrics metrics = new SearchMetrics(meterRegistry);
        SearchMetricsReporter reporter = new SearchMetricsReporter(cacheManager, meterRegistry);
        Cache cache = cacheManager.getCache("queryEmbeddings");

        cache.get("반도체");
        cache.put("반도체", new float[768]);
        cache.get("반도체");
        long startedAt = System.nanoTime() - 1_000_000;
        metrics.recordTotal(startedAt);
        metrics.recordEmbeddingLookup(startedAt);
        metrics.recordEmbeddingApi(startedAt);
        metrics.recordHybridDatabase(startedAt);

        SearchMetricsReporter.Snapshot snapshot = reporter.snapshot();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.searchCount()).isEqualTo(1);
        assertThat(snapshot.embeddingApiCalls()).isEqualTo(1);
        assertThat(snapshot.cacheHits()).isEqualTo(1);
        assertThat(snapshot.cacheMisses()).isEqualTo(1);
        assertThat(snapshot.cacheHitRate()).isEqualTo(0.5);
    }
}
