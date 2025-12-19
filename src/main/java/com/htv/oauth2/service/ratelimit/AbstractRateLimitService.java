package com.htv.oauth2.service.ratelimit;

import com.htv.oauth2.dto.ratelimit.RateLimitResult;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public abstract class AbstractRateLimitService {

    protected final ProxyManager<String> proxyManager;
    protected final String keyPrefix;

    protected AbstractRateLimitService() {
        this(null, null);
    }

    protected AbstractRateLimitService(ProxyManager<String> proxyManager, String keyPrefix) {
        this.proxyManager = proxyManager;
        this.keyPrefix = keyPrefix;
    }

    /**
     * Check rate limit for given identifier
     */
    public RateLimitResult checkLimit(String identifier) {
        String key = keyPrefix + identifier;
        Supplier<BucketConfiguration> configSupplier = this::createBucketConfiguration;

        return performRateCheck(key, configSupplier, getServiceName(), identifier);
    }

    /**
     * Reset rate limit for given identifier
     */
    public void resetLimit(String identifier) {
        String key = keyPrefix + identifier;
        try {
            proxyManager.removeProxy(key);
            log.debug("Reset {} rate limit for: {}", getServiceName(), identifier);
        } catch (Exception e) {
            log.error("Failed to reset {} limit for: {}", getServiceName(), identifier, e);
        }
    }

    /**
     * Get remaining capacity
     */
    public long getRemainingCapacity(String identifier) {
        String key = keyPrefix + identifier;
        try {
            Bucket bucket = proxyManager.builder().build(key, this::createBucketConfiguration);
            return bucket.getAvailableTokens();
        } catch (Exception e) {
            log.error("Failed to get remaining capacity for: {}", identifier, e);
            return getCapacity();
        }
    }

    /**
     * Core rate limit check logic
     */
    private RateLimitResult performRateCheck(
            String key,
            Supplier<BucketConfiguration> configSupplier,
            String serviceName,
            String identifier
    ) {
        try {
            Bucket bucket = proxyManager.builder().build(key, configSupplier);
            var probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                log.debug("{} rate limit check passed for: {} (remaining: {})",
                        serviceName, identifier, probe.getRemainingTokens());

                return RateLimitResult.allowed(probe.getRemainingTokens());
            } else {
                long waitTime = probe.getNanosToWaitForRefill() / 1_000_000_000;

                log.warn("{} rate limit exceeded for: {}. Retry after: {}s",
                        serviceName, identifier, waitTime);

                return RateLimitResult.blocked(waitTime);
            }

        } catch (Exception e) {
            log.error("Rate limit check failed for key: {}. Allowing by default (fail-open).", key, e);
            return RateLimitResult.allowed(Integer.MAX_VALUE);
        }
    }

    // Abstract methods to be implemented by subclasses
    protected abstract BucketConfiguration createBucketConfiguration();
    protected abstract String getServiceName();
    protected abstract int getCapacity();

    /**
     * Helper method to create bandwidth with interval refill
     */
    protected Bandwidth createIntervalBandwidth(int capacity, long refillPeriodSeconds) {
        return Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, java.time.Duration.ofSeconds(refillPeriodSeconds))
                .initialTokens(capacity)
                .build();
    }

    /**
     * Helper method to create bandwidth with greedy refill
     */
    protected Bandwidth createGreedyBandwidth(int capacity, long refillPeriodSeconds) {
        return Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, java.time.Duration.ofSeconds(refillPeriodSeconds))
                .initialTokens(capacity)
                .build();
    }
}