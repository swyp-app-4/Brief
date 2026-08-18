package com.brife.news.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                build("top5News",          500,   7,  TimeUnit.HOURS),
                build("queryEmbeddings",   10000, 1,  TimeUnit.HOURS),
                build("searchSuggestions", 10000, 5,  TimeUnit.MINUTES),
                build("queryCorrections",  10000, 1,  TimeUnit.HOURS)
        ));
        return manager;
    }

    private CaffeineCache build(String name, int maxSize, long duration, TimeUnit unit) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(duration, unit)
                        .build());
    }
}
