package com.htv.oauth2.service.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class LoginRateLimitService extends AbstractRateLimitService {

    private static final String KEY_PREFIX = "bucket:login:";

    @ConfigProperty(name = "oauth2.rate-limit.login.capacity", defaultValue = "5")
    int capacity;

    @ConfigProperty(name = "oauth2.rate-limit.login.refill-period", defaultValue = "300")
    int refillPeriod;

    @ConfigProperty(name = "oauth2.rate-limit.login.burst-capacity", defaultValue = "2")
    int burstCapacity;

    @Inject
    public LoginRateLimitService(ProxyManager<String> proxyManager) {
        super(proxyManager, KEY_PREFIX);
    }

    @Override
    protected BucketConfiguration createBucketConfiguration() {
        return BucketConfiguration.builder()
                // Main limit: 5 requests per 5 minutes
                .addLimit(createIntervalBandwidth(capacity, refillPeriod))
                // Burst limit: Allow 2 extra requests initially
                .addLimit(createIntervalBandwidth(
                        capacity + burstCapacity,
                        refillPeriod * 2
                ))
                .build();
    }

    @Override
    protected String getServiceName() {
        return "Login";
    }

    @Override
    protected int getCapacity() {
        return capacity;
    }
}