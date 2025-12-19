package com.htv.oauth2.dto.ratelimit;

import lombok.Getter;

@Getter
public class RateLimitResult {
    private final boolean allowed;
    private final long remainingTokens;
    private final long retryAfterSeconds;

    private RateLimitResult(boolean allowed, long remainingTokens, long retryAfterSeconds) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public static RateLimitResult allowed(long remaining) {
        return new RateLimitResult(true, remaining, 0);
    }

    public static RateLimitResult blocked(long retryAfter) {
        return new RateLimitResult(false, 0, retryAfter);
    }

    public boolean isBlocked() {
        return !allowed;
    }
}
