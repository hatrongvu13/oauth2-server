package com.htv.oauth2.util;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class JwtUtil {

    @ConfigProperty(name = "oauth2.jwt.issuer")
    String issuer;

    @ConfigProperty(name = "oauth2.jwt.access-token-expiry", defaultValue = "3600")
    Long accessTokenExpiry;

    /**
     * Generate Access Token (JWT)
     */
    public String generateAccessToken(String userId, String clientId, Set<String> scopes) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiry);

        try {
            return Jwt.issuer(issuer)
                    .subject(userId)
                    .audience(clientId)
                    .issuedAt(now)
                    .expiresAt(expiresAt)
                    .claim("scope", String.join(" ", scopes))
                    .claim("client_id", clientId)
                    .claim("token_type", "access_token")
                    .jws()
                    .sign(); // Will use configured private key

        } catch (Exception e) {
            log.error("Failed to generate access token", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Generate ID Token
     */
    public String generateIdToken(String userId, String clientId, String email, String name) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiry);

        try {
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
        } catch (Exception e) {
            log.error("Failed to sign ID token: {}", e.getMessage());
            throw new RuntimeException("ID token signing failed", e);
        }
    }

    public String generateRefreshToken() {
        return CryptoUtil.generateSecureToken(64);
    }

    public boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        return token.split("\\.").length == 3;
    }
}