package com.htv.oauth2.cache;

import com.htv.oauth2.service.CacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class SessionCache {

    @Inject
    CacheService cacheService;

    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_TTL = 1800; // 30 minutes

    /**
     * Create session
     */
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;
        cacheService.put(key, userId, SESSION_TTL);
        log.debug("Created session for user: {}", userId);
        return sessionId;
    }

    /**
     * Get user ID from session
     */
    public Optional<String> getUserIdFromSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return cacheService.get(key);
    }

    /**
     * Extend session TTL
     */
    public void extendSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        cacheService.setExpire(key, SESSION_TTL);
        log.debug("Extended session: {}", sessionId);
    }

    /**
     * Invalidate session
     */
    public void invalidateSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        cacheService.delete(key);
        log.debug("Invalidated session: {}", sessionId);
    }
}
