package com.htv.oauth2.security;

import com.htv.oauth2.service.CacheService;
import com.htv.oauth2.util.CryptoUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SessionManager {

    @Inject
    CacheService cacheService;

    private static final int SESSION_TTL = 3600; // 1 hour

    /**
     * Create session for user
     */
    public String createSession(String userId) {
        String sessionId = CryptoUtil.generateSecureToken(32);
        String key = "session:" + sessionId;

        cacheService.put(key, userId, SESSION_TTL);
        log.info("Session created for user: {}", userId);

        return sessionId;
    }

    /**
     * Get user ID from session
     */
    public String getUserIdFromSession(String sessionId) {
        String key = "session:" + sessionId;
        return cacheService.get(key).orElse(null);
    }

    /**
     * Delete session
     */
    public void deleteSession(String sessionId) {
        String key = "session:" + sessionId;
        cacheService.delete(key);
        log.info("Session deleted: {}", sessionId);
    }

    /**
     * Extend session TTL
     */
    public void extendSession(String sessionId) {
        String key = "session:" + sessionId;
        cacheService.get(key).ifPresent(userId -> {
            cacheService.put(key, userId, SESSION_TTL);
        });
    }
}
