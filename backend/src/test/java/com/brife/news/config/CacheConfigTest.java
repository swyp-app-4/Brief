package com.brife.news.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void queryEmbeddingCacheRecordsHitsAndMisses() {
        SimpleCacheManager manager = (SimpleCacheManager) new CacheConfig().cacheManager();
        manager.afterPropertiesSet();
        CaffeineCache springCache = (CaffeineCache) manager.getCache("queryEmbeddings");
        assertThat(springCache).isNotNull();
        Cache<Object, Object> cache = springCache.getNativeCache();

        cache.getIfPresent("반도체");
        cache.put("반도체", new float[768]);
        cache.getIfPresent("반도체");

        assertThat(cache.stats().missCount()).isEqualTo(1);
        assertThat(cache.stats().hitCount()).isEqualTo(1);
    }
}
