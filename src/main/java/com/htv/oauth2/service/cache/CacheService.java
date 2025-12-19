package com.htv.oauth2.service.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@ApplicationScoped
public class CacheService {

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;
    private final RedisDataSource redisDataSource;

    @Inject
    public CacheService(RedisDataSource redisDataSource) {
        this.redisDataSource = redisDataSource;
        this.valueCommands = redisDataSource.value(String.class, String.class);
        this.keyCommands = redisDataSource.key(String.class);
    }

    @PostConstruct
    void init() {
        try {
            String response = redisDataSource.execute("PING").toString();
            log.info("✅ Cache service initialized successfully: {}", response);
        } catch (Exception e) {
            log.error("❌ Cannot connect to Redis server: {}", e.getMessage());
            throw new RuntimeException("Redis connection failed", e);
        }
    }

    // ============= Basic Operations =============

    public void put(String key, String value, long ttlSeconds) {
        execute(() -> {
            valueCommands.setex(key, ttlSeconds, value);
            log.debug("Cached: {} with TTL: {}s", key, ttlSeconds);
            return null;
        }, "put");
    }

    public void put(String key, String value) {
        execute(() -> {
            valueCommands.set(key, value);
            log.debug("Cached: {} (persistent)", key);
            return null;
        }, "put");
    }

    public Optional<String> get(String key) {
        return execute(() -> {
            String value = valueCommands.get(key);
            log.debug("Retrieved: {} - found: {}", key, value != null);
            return Optional.ofNullable(value);
        }, "get").orElse(Optional.empty());
    }

    public void delete(String key) {
        execute(() -> {
            valueCommands.getdel(key);
            log.debug("Deleted: {}", key);
            return null;
        }, "delete");
    }

    public void deletePattern(String pattern) {
        execute(() -> {
            List<String> keys = keyCommands.keys(pattern);
            keys.forEach(this::delete);
            log.info("Deleted {} keys matching: {}", keys.size(), pattern);
            return null;
        }, "deletePattern");
    }

    public boolean exists(String key) {
        try {
            return valueCommands.get(key) != null;
        } catch (Exception e) {
            log.error("Failed to check if key exists: {}", key, e);
            return false;
        }
    }

    // ============= Counter Operations =============

    public long increment(String key, long ttlSeconds) {
        return execute(() -> {
            long value = valueCommands.incr(key);
            if (value == 1) {
                keyCommands.expire(key, Duration.ofSeconds(ttlSeconds));
                log.debug("Counter initialized: {} with TTL: {}s", key, ttlSeconds);
            }
            return value;
        }, "increment").orElse(0L);
    }

    public long decrement(String key) {
        return execute(() -> valueCommands.decr(key), "decrement")
                .orElse(0L);
    }

    public long incrementBy(String key, long delta, long ttlSeconds) {
        return execute(() -> {
            long value = valueCommands.incrby(key, delta);
            if (value == delta) {
                keyCommands.expire(key, Duration.ofSeconds(ttlSeconds));
            }
            return value;
        }, "incrementBy").orElse(0L);
    }

    // ============= TTL Operations =============

    public boolean setExpire(String key, long ttlSeconds) {
        return execute(() -> {
            boolean result = keyCommands.expire(key, Duration.ofSeconds(ttlSeconds));
            log.debug("Set expiration {}s for: {} - success: {}", ttlSeconds, key, result);
            return result;
        }, "setExpire").orElse(false);
    }

    public Optional<Long> getTTL(String key) {
        return execute(() -> {
            long ttl = keyCommands.ttl(key);
            return ttl >= 0 ? Optional.of(ttl) : Optional.<Long>empty();
        }, "getTTL").orElse(Optional.empty());
    }

    // ============= Advanced Operations =============

    public boolean setIfAbsent(String key, String value, long ttlSeconds) {
        return execute(() -> {
            boolean result = valueCommands.setnx(key, value);
            if (result && ttlSeconds > 0) {
                keyCommands.expire(key, Duration.ofSeconds(ttlSeconds));
            }
            return result;
        }, "setIfAbsent").orElse(false);
    }

    public Optional<String> getAndSet(String key, String value) {
        return execute(() -> Optional.ofNullable(valueCommands.getset(key, value)), "getAndSet")
                .orElse(Optional.empty());
    }

    public List<String> multiGet(String... keys) {
        return execute(() -> List.copyOf(valueCommands.mget(keys).values()), "multiGet")
                .orElse(List.of());
    }

    // ============= Utility Methods =============

    /**
     * Get or compute value if not present
     */
    public String getOrCompute(String key, long ttlSeconds, Supplier<String> supplier) {
        Optional<String> cached = get(key);
        if (cached.isPresent()) {
            return cached.get();
        }

        String value = supplier.get();
        put(key, value, ttlSeconds);
        return value;
    }

    public void flushAll() {
        execute(() -> {
            redisDataSource.execute("FLUSHDB");
            log.warn("⚠️ Flushed all Redis cache");
            return null;
        }, "flushAll");
    }

    public String getInfo() {
        return execute(() -> redisDataSource.execute("INFO").toString(), "getInfo")
                .orElse("Error getting Redis info");
    }

    // ============= Error Handling Wrapper =============

    private <T> Optional<T> execute(Supplier<T> operation, String operationName) {
        try {
            return Optional.ofNullable(operation.get());
        } catch (Exception e) {
            log.error("Failed to execute {} operation", operationName, e);
            return Optional.empty();
        }
    }
}