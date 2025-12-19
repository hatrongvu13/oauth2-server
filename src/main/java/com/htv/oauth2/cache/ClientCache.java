package com.htv.oauth2.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htv.oauth2.domain.Client;
import com.htv.oauth2.service.cache.CacheService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ClientCache {

    @Inject
    CacheService cacheService;

    @Inject
    ObjectMapper objectMapper;

    private static final String CLIENT_PREFIX = "client:";
    private static final long DEFAULT_TTL = 1800; // 30 minutes

    /**
     * Cache client
     */
    public void cacheClient(Client client) {
        try {
            String key = CLIENT_PREFIX + client.getClientId();
            String json = objectMapper.writeValueAsString(client);
            cacheService.put(key, json, DEFAULT_TTL);
            log.debug("Cached client: {}", client.getClientId());
        } catch (Exception e) {
            log.error("Failed to cache client", e);
        }
    }

    /**
     * Get client from cache
     */
    public Optional<Client> getClient(String clientId) {
        try {
            String key = CLIENT_PREFIX + clientId;
            return cacheService.get(key)
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, Client.class);
                        } catch (Exception e) {
                            log.error("Failed to deserialize client", e);
                            return null;
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to get cached client", e);
            return Optional.empty();
        }
    }

    /**
     * Invalidate client cache
     */
    public void invalidateClient(String clientId) {
        String key = CLIENT_PREFIX + clientId;
        cacheService.delete(key);
        log.debug("Invalidated client cache: {}", clientId);
    }
}
