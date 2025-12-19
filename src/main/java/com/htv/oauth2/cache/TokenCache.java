package com.htv.oauth2.cache;

import com.htv.oauth2.service.cache.CacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@ApplicationScoped
public class TokenCache {

    @Inject
    CacheService cacheService;

    private static final String TOKEN_PREFIX = "token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long DEFAULT_TTL = 3600; // 1 hour

    /**
     * Cache access token metadata
     */
    public void cacheAccessToken(String token, String userId, String clientId, long ttlSeconds) {
        try {
            String key = TOKEN_PREFIX + token;
            String value = String.format("%s:%s:%d", userId, clientId, Instant.now().getEpochSecond());
            cacheService.put(key, value, ttlSeconds);
            log.debug("Cached access token for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to cache access token", e);
        }
    }

    /**
     * Get cached token info
     */
    public TokenCacheInfo getAccessToken(String token) {
        try {
            String key = TOKEN_PREFIX + token;
            return cacheService.get(key)
                    .map(this::parseTokenInfo)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to get cached token", e);
            return null;
        }
    }

    /**
     * Invalidate access token
     */
    public void invalidateAccessToken(String token) {
        String key = TOKEN_PREFIX + token;
        cacheService.delete(key);
        log.debug("Invalidated access token from cache");
    }

    /**
     * Cache refresh token
     */
    public void cacheRefreshToken(String refreshToken, String accessTokenId, long ttlSeconds) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            cacheService.put(key, accessTokenId, ttlSeconds);
            log.debug("Cached refresh token");
        } catch (Exception e) {
            log.error("Failed to cache refresh token", e);
        }
    }

    /**
     * Get refresh token info
     */
    public String getRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        return cacheService.get(key).orElse(null);
    }

    /**
     * Invalidate refresh token
     */
    public void invalidateRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        cacheService.delete(key);
        log.debug("Invalidated refresh token from cache");
    }

    private TokenCacheInfo parseTokenInfo(String value) {
        try {
            String[] parts = value.split(":");
            if (parts.length >= 3) {
                return new TokenCacheInfo(parts[0], parts[1], Long.parseLong(parts[2]));
            }
        } catch (Exception e) {
            log.warn("Failed to parse token cache info: {}", value);
        }
        return null;
    }

    public record TokenCacheInfo(String userId, String clientId, long cachedAt) {
    }
}


