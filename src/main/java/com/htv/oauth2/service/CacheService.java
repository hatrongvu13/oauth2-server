package com.htv.oauth2.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class CacheService {

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;

    @Inject
    public CacheService(RedisDataSource dataSource) {
        this.valueCommands = dataSource.value(String.class, String.class);
        this.keyCommands = dataSource.key(String.class);
    }

    /**
     * Store value in cache with TTL
     */
    public void put(String key, String value, long ttlSeconds) {
        try {
            valueCommands.setex(key, ttlSeconds, value);
            log.debug("Cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to cache value for key: {}", key, e);
        }
    }

    /**
     * Get value from cache
     */
    public Optional<String> get(String key) {
        try {
            String value = valueCommands.get(key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Failed to get cached value for key: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Delete value from cache
     */
    public void delete(String key) {
        try {
            valueCommands.getdel(key);
            log.debug("Deleted cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete cached value for key: {}", key, e);
        }
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        try {
            return valueCommands.get(key) != null;
        } catch (Exception e) {
            log.error("Failed to check if key exists: {}", key, e);
            return false;
        }
    }

    /**
     * Increment counter (for rate limiting)
     */
    public long increment(String key, long ttlSeconds) {
        try {
            long value = valueCommands.incr(key);
            if (value == 1) {
                // First increment, set TTL using KeyCommands
                keyCommands.expire(key, Duration.ofSeconds(ttlSeconds));
            }
            return value;
        } catch (Exception e) {
            log.error("Failed to increment counter for key: {}", key, e);
            return 0;
        }
    }

    /**
     * Set expiration time for existing key
     */
    public boolean setExpire(String key, long ttlSeconds) {
        try {
            return keyCommands.expire(key, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Failed to set expiration for key: {}", key, e);
            return false;
        }
    }

    /**
     * Get remaining TTL for key
     */
    public Optional<Long> getTTL(String key) {
        try {
            Duration ttl = Duration.ofDays(keyCommands.ttl(key));
            return Optional.of(ttl.getSeconds());
        } catch (Exception e) {
            log.error("Failed to get TTL for key: {}", key, e);
            return Optional.empty();
        }
    }
}