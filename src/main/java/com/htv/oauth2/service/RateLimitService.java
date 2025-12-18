package com.htv.oauth2.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
public class RateLimitService {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> mfaBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> emailBuckets = new ConcurrentHashMap<>();

    @ConfigProperty(name = "oauth2.rate-limit.login.capacity", defaultValue = "5")
    int loginCapacity;

    @ConfigProperty(name = "oauth2.rate-limit.login.refill-period", defaultValue = "300")
    int loginRefillPeriod;

    @ConfigProperty(name = "oauth2.rate-limit.mfa.capacity", defaultValue = "3")
    int mfaCapacity;

    @ConfigProperty(name = "oauth2.rate-limit.mfa.refill-period", defaultValue = "60")
    int mfaRefillPeriod;

    @ConfigProperty(name = "oauth2.rate-limit.email.capacity", defaultValue = "10")
    int emailCapacity;

    @ConfigProperty(name = "oauth2.rate-limit.email.refill-period", defaultValue = "3600")
    int emailRefillPeriod;

    /**
     * Check login rate limit
     */
    public boolean allowLogin(String identifier) {
        Bucket bucket = loginBuckets.computeIfAbsent(identifier, k -> createLoginBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Login rate limit exceeded for: {}", identifier);
        }

        return allowed;
    }

    /**
     * Check MFA rate limit
     */
    public boolean allowMfaAttempt(String userId) {
        Bucket bucket = mfaBuckets.computeIfAbsent(userId, k -> createMfaBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("MFA rate limit exceeded for: {}", userId);
        }

        return allowed;
    }

    /**
     * Check email rate limit
     */
    public boolean allowEmail(String email) {
        Bucket bucket = emailBuckets.computeIfAbsent(email, k -> createEmailBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Email rate limit exceeded for: {}", email);
        }

        return allowed;
    }

    /**
     * Reset login attempts (after successful login)
     */
    public void resetLoginAttempts(String identifier) {
        loginBuckets.remove(identifier);
        log.debug("Reset login attempts for: {}", identifier);
    }

    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(
                loginCapacity,
                Refill.intervally(loginCapacity, Duration.ofSeconds(loginRefillPeriod))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createMfaBucket() {
        Bandwidth limit = Bandwidth.classic(
                mfaCapacity,
                Refill.intervally(mfaCapacity, Duration.ofSeconds(mfaRefillPeriod))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createEmailBucket() {
        Bandwidth limit = Bandwidth.classic(
                emailCapacity,
                Refill.intervally(emailCapacity, Duration.ofSeconds(emailRefillPeriod))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}