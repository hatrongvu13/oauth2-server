package com.htv.oauth2.cache;

import com.htv.oauth2.service.cache.CacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AuthorizationCodeCache {

    @Inject
    CacheService cacheService;

    private static final String AUTH_CODE_PREFIX = "auth_code:";
    private static final long DEFAULT_TTL = 300; // 5 minutes

    /**
     * Cache authorization code metadata
     */
    public void cacheAuthCode(String code, String userId, String clientId) {
        String key = AUTH_CODE_PREFIX + code;
        String value = String.format("%s:%s", userId, clientId);
        cacheService.put(key, value, DEFAULT_TTL);
        log.debug("Cached authorization code");
    }

    /**
     * Get authorization code info
     */
    public AuthCodeInfo getAuthCode(String code) {
        String key = AUTH_CODE_PREFIX + code;
        return cacheService.get(key)
                .map(value -> {
                    String[] parts = value.split(":");
                    if (parts.length == 2) {
                        return new AuthCodeInfo(parts[0], parts[1]);
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * Invalidate authorization code
     */
    public void invalidateAuthCode(String code) {
        String key = AUTH_CODE_PREFIX + code;
        cacheService.delete(key);
        log.debug("Invalidated authorization code");
    }

    public record AuthCodeInfo(String userId, String clientId) {}
}
