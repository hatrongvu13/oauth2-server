package com.htv.oauth2.service.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailRateLimitService extends AbstractRateLimitService {

    private static final String KEY_PREFIX = "bucket:email:";

    @ConfigProperty(name = "oauth2.rate-limit.email.capacity", defaultValue = "10")
    int capacity;

    @ConfigProperty(name = "oauth2.rate-limit.email.refill-period", defaultValue = "3600")
    int refillPeriod;

    @Inject
    public EmailRateLimitService(ProxyManager<String> proxyManager) {
        super(proxyManager, KEY_PREFIX);
    }

    @Override
    protected BucketConfiguration createBucketConfiguration() {
        return BucketConfiguration.builder()
                // Greedy refill: Smooth refill over time
                // 10 emails per hour = 1 token every 6 minutes
                .addLimit(createGreedyBandwidth(capacity, refillPeriod))
                .build();
    }

    @Override
    protected String getServiceName() {
        return "Email";
    }

    @Override
    protected int getCapacity() {
        return capacity;
    }
}