package com.brife.news.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SemanticDuplicatePolicy {

    private final boolean enabled;
    private final boolean blockingEnabled;

    public SemanticDuplicatePolicy(
            @Value("${news.duplicate.semantic-enabled:true}") boolean enabled,
            @Value("${news.duplicate.semantic-blocking-enabled:false}") boolean blockingEnabled) {
        this.enabled = enabled;
        this.blockingEnabled = blockingEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isBlockingEnabled() {
        return blockingEnabled;
    }
}
