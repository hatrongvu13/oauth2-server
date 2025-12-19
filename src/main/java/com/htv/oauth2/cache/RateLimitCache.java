package com.htv.oauth2.cache;

import com.htv.oauth2.service.cache.CacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RateLimitCache {

    @Inject
    CacheService cacheService;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * Increment rate limit counter
     */
    public long increment(String key, long windowSeconds) {
        String fullKey = RATE_LIMIT_PREFIX + key;
        return cacheService.increment(fullKey, windowSeconds);
    }

    /**
     * Get current count
     */
    public long getCount(String key) {
        String fullKey = RATE_LIMIT_PREFIX + key;
        return cacheService.get(fullKey)
                .map(Long::parseLong)
                .orElse(0L);
    }

    /**
     * Reset rate limit
     */
    public void reset(String key) {
        String fullKey = RATE_LIMIT_PREFIX + key;
        cacheService.delete(fullKey);
        log.debug("Reset rate limit: {}", key);
    }

    /**
     * Get remaining TTL
     */
    public long getRemainingTTL(String key) {
        String fullKey = RATE_LIMIT_PREFIX + key;
        return cacheService.getTTL(fullKey).orElse(0L);
    }
}
