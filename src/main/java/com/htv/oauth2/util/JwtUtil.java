package com.htv.oauth2.util;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

import static com.htv.oauth2.util.CryptoUtil.generateSecureToken;

@Slf4j
@ApplicationScoped
public class JwtUtil {

    @ConfigProperty(name = "oauth2.jwt.issuer", defaultValue = "https://oauth2.htv.com")
    String issuer;

    @ConfigProperty(name = "oauth2.jwt.access-token-expiry", defaultValue = "3600") // 1 hour
    Long accessTokenExpiry;

    @ConfigProperty(name = "oauth2.jwt.refresh-token-expiry", defaultValue = "86400") // 24 hours
    Long refreshTokenExpiry;

    /**
     * Generate Access Token (JWT)
     */
    public String generateAccessToken(String userId, String clientId, Set<String> scopes) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiry);

        return Jwt.issuer(issuer)
                .subject(userId)
                .audience(clientId)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("scope", String.join(" ", scopes))
                .claim("client_id", clientId)
                .claim("token_type", "access_token")
                .jws()
                .sign();
    }

    /**
     * Generate Refresh Token (JWT or random string)
     */
    public String generateRefreshToken() {
        // For simplicity, using secure random string
        // In production, you might want to use JWT with longer expiry
        return generateSecureToken(64);
    }

    /**
     * Generate ID Token (for OpenID Connect)
     */
    public String generateIdToken(String userId, String clientId, String email, String name) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiry);

        return Jwt.issuer(issuer)
                .subject(userId)
                .audience(clientId)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", email)
                .claim("name", name)
                .claim("email_verified", true)
                .jws()
                .sign();
    }

    /**
     * Validate JWT structure (basic check)
     */
    public boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    /**
     * Extract claims from JWT (without verification)
     * Use only for logging/debugging
     */
    public Map<String, Object> extractClaimsUnsafe(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return Collections.emptyMap();
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );

            // Parse JSON manually or use a library
            // This is a simplified version
            return Collections.emptyMap(); // Implement JSON parsing
        } catch (Exception e) {
            log.error("Failed to extract claims from JWT", e);
            return Collections.emptyMap();
        }
    }
}