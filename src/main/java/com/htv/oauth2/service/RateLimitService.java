package com.htv.oauth2.service;

import com.htv.oauth2.dto.ratelimit.RateLimitResult;
import com.htv.oauth2.service.ratelimit.EmailRateLimitService;
import com.htv.oauth2.service.ratelimit.LoginRateLimitService;
import com.htv.oauth2.service.ratelimit.MfaRateLimitService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
public class RateLimitService {

    @Inject
    LoginRateLimitService loginRateLimitService;

    @Inject
    MfaRateLimitService mfaRateLimitService;

    @Inject
    EmailRateLimitService emailRateLimitService;

    // ============= Login =============

    public RateLimitResult checkLogin(String identifier) {
        return loginRateLimitService.checkLimit(identifier);
    }

    public void resetLogin(String identifier) {
        loginRateLimitService.resetLimit(identifier);
    }

    public long getRemainingLoginAttempts(String identifier) {
        return loginRateLimitService.getRemainingCapacity(identifier);
    }

    // ============= MFA =============

    public RateLimitResult checkMfa(String userId) {
        return mfaRateLimitService.checkLimit(userId);
    }

    public void resetMfa(String userId) {
        mfaRateLimitService.resetLimit(userId);
    }

    public long getRemainingMfaAttempts(String userId) {
        return mfaRateLimitService.getRemainingCapacity(userId);
    }

    // ============= Email =============

    public RateLimitResult checkEmail(String email) {
        return emailRateLimitService.checkLimit(email);
    }

    public long getRemainingEmailQuota(String email) {
        return emailRateLimitService.getRemainingCapacity(email);
    }

    // ============= Batch Operations =============

    public void resetAllForUser(String identifier) {
        loginRateLimitService.resetLimit(identifier);
        mfaRateLimitService.resetLimit(identifier);
        emailRateLimitService.resetLimit(identifier);
        log.info("Reset all rate limits for: {}", identifier);
    }
}