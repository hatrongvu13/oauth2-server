package com.htv.oauth2.exception.security;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;
import lombok.Getter;

@Getter
public class RateLimitExceededException extends OAuth2Exception {

    private final long retryAfter; // seconds

    public RateLimitExceededException(String message, long retryAfter) {
        super("rate_limit_exceeded", message, 429);
        this.retryAfter = retryAfter;
    }

    public RateLimitExceededException(long retryAfter) {
        super("rate_limit_exceeded", "Rate limit exceeded. Please try again in " + retryAfter + " seconds.", 429);
        this.retryAfter = retryAfter;
    }

    public long getRetryAfterSeconds() {
        return retryAfter;
    }
}
