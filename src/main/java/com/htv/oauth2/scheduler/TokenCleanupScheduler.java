package com.htv.oauth2.scheduler;

import com.htv.oauth2.repository.*;
import com.htv.oauth2.service.auth.AuthorizationService;
import com.htv.oauth2.service.token.TokenService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class TokenCleanupScheduler {

    @Inject
    TokenService tokenService;

    @Inject
    AuthorizationService authorizationService;

    /**
     * Clean up expired tokens every hour
     */
    @Scheduled(every = "1h", identity = "token-cleanup")
    void cleanupExpiredTokens() {
        log.info("Starting token cleanup job");
        try {
            tokenService.cleanupExpiredTokens();
            log.info("Token cleanup completed successfully");
        } catch (Exception e) {
            log.error("Token cleanup failed", e);
        }
    }

    /**
     * Clean up expired authorization codes every 15 minutes
     */
    @Scheduled(every = "15m", identity = "authcode-cleanup")
    void cleanupExpiredAuthorizationCodes() {
        log.info("Starting authorization code cleanup job");
        try {
            authorizationService.cleanupExpiredAuthorizationCodes();
            log.info("Authorization code cleanup completed successfully");
        } catch (Exception e) {
            log.error("Authorization code cleanup failed", e);
        }
    }
}


