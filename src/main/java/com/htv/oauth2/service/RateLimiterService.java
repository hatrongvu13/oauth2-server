//package com.htv.oauth2.service;
//
//import com.htv.oauth2.exception.RateLimitExceededException;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.microprofile.config.inject.ConfigProperty;
//
//@Slf4j
//@ApplicationScoped
//public class RateLimiterService {
//
//    @Inject
//    CacheService cacheService;
//
//    @ConfigProperty(name = "oauth2.rate-limit.login.max-attempts", defaultValue = "5")
//    int maxLoginAttempts;
//
//    @ConfigProperty(name = "oauth2.rate-limit.login.window-seconds", defaultValue = "900")
//    long loginWindowSeconds;
//
//    @ConfigProperty(name = "oauth2.rate-limit.token.max-requests", defaultValue = "100")
//    int maxTokenRequests;
//
//    @ConfigProperty(name = "oauth2.rate-limit.token.window-seconds", defaultValue = "3600")
//    long tokenWindowSeconds;
//
//    /**
//     * Check login rate limit for IP address
//     */
//    public void checkLoginRateLimit(String ipAddress) {
//        String key = "rate_limit:login:" + ipAddress;
//        long attempts = cacheService.increment(key, loginWindowSeconds);
//
//        if (attempts > maxLoginAttempts) {
//            long retryAfter = loginWindowSeconds;
//            throw new RateLimitExceededException(
//                    "Too many login attempts. Please try again later.",
//                    retryAfter
//            );
//        }
//
//        log.debug("Login attempts for IP {}: {}/{}", ipAddress, attempts, maxLoginAttempts);
//    }
//
//    /**
//     * Check token endpoint rate limit for client
//     */
//    public void checkTokenRateLimit(String clientId) {
//        String key = "rate_limit:token:" + clientId;
//        long requests = cacheService.increment(key, tokenWindowSeconds);
//
//        if (requests > maxTokenRequests) {
//            long retryAfter = tokenWindowSeconds;
//            throw new RateLimitExceededException(
//                    "Too many token requests. Please try again later.",
//                    retryAfter
//            );
//        }
//
//        log.debug("Token requests for client {}: {}/{}", clientId, requests, maxTokenRequests);
//    }
//
//    /**
//     * Reset rate limit for key
//     */
//    public void resetRateLimit(String key) {
//        cacheService.delete(key);
//        log.info("Rate limit reset for key: {}", key);
//    }
//}