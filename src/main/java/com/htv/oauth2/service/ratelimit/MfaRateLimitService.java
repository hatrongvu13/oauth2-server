package com.htv.oauth2.service.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MfaRateLimitService extends AbstractRateLimitService {

    private static final String KEY_PREFIX = "bucket:mfa:";

    @ConfigProperty(name = "oauth2.rate-limit.mfa.capacity", defaultValue = "3")
    int capacity;

    @ConfigProperty(name = "oauth2.rate-limit.mfa.refill-period", defaultValue = "60")
    int refillPeriod;

    @Inject
    public MfaRateLimitService(ProxyManager<String> proxyManager) {
        super(proxyManager, KEY_PREFIX);
    }

    @Override
    protected BucketConfiguration createBucketConfiguration() {
        return BucketConfiguration.builder()
                // Strict limit: 3 requests per minute (no burst)
                .addLimit(createIntervalBandwidth(capacity, refillPeriod))
                .build();
    }

    @Override
    protected String getServiceName() {
        return "MFA";
    }

    @Override
    protected int getCapacity() {
        return capacity;
    }
}