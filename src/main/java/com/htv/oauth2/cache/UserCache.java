package com.htv.oauth2.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htv.oauth2.domain.User;
import com.htv.oauth2.service.CacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@ApplicationScoped
public class UserCache {

    @Inject
    CacheService cacheService;

    @Inject
    ObjectMapper objectMapper;

    private static final String USER_PREFIX = "user:";
    private static final String USER_BY_USERNAME_PREFIX = "user:username:";
    private static final long DEFAULT_TTL = 600; // 10 minutes

    /**
     * Cache user by ID
     */
    public void cacheUser(User user) {
        try {
            String key = USER_PREFIX + user.getId();
            String json = objectMapper.writeValueAsString(user);
            cacheService.put(key, json, DEFAULT_TTL);

            // Also cache by username for quick lookup
            String usernameKey = USER_BY_USERNAME_PREFIX + user.getUsername();
            cacheService.put(usernameKey, user.getId(), DEFAULT_TTL);

            log.debug("Cached user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to cache user", e);
        }
    }

    /**
     * Get user from cache by ID
     */
    public Optional<User> getUser(String userId) {
        try {
            String key = USER_PREFIX + userId;
            return cacheService.get(key)
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, User.class);
                        } catch (Exception e) {
                            log.error("Failed to deserialize user", e);
                            return null;
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to get cached user", e);
            return Optional.empty();
        }
    }

    /**
     * Get user ID by username
     */
    public Optional<String> getUserIdByUsername(String username) {
        String key = USER_BY_USERNAME_PREFIX + username;
        return cacheService.get(key);
    }

    /**
     * Invalidate user cache
     */
    public void invalidateUser(String userId) {
        String key = USER_PREFIX + userId;
        cacheService.delete(key);
        log.debug("Invalidated user cache: {}", userId);
    }

    /**
     * Invalidate user cache by username
     */
    public void invalidateUserByUsername(String username) {
        String key = USER_BY_USERNAME_PREFIX + username;
        cacheService.delete(key);
        log.debug("Invalidated user cache by username: {}", username);
    }
}
