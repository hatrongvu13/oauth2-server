package com.htv.oauth2.mapper;

import com.htv.oauth2.domain.AccessToken;
import com.htv.oauth2.dto.response.TokenIntrospectionResponse;
import com.htv.oauth2.dto.response.TokenResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.Instant;
import java.util.Set; /**
 * Manual implementation of TokenMapper
 */
@ApplicationScoped
public class TokenMapper {

    public TokenResponse toTokenResponse(AccessToken accessToken) {
        if (accessToken == null) return null;

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .tokenType("Bearer")
                .expiresIn(calculateExpiresIn(accessToken.getExpiresAt()))
                .scope(joinScopes(accessToken.getScopes()))
                .build();
    }

    public TokenIntrospectionResponse toIntrospectionResponse(AccessToken accessToken) {
        if (accessToken == null) return null;

        return TokenIntrospectionResponse.builder()
                .active(true)
                .scope(joinScopes(accessToken.getScopes()))
                .clientId(accessToken.getClientId())
                .username(accessToken.getUser() != null ? accessToken.getUser().getUsername() : null)
                .tokenType("Bearer")
                .exp(toEpochSecond(accessToken.getExpiresAt()))
                .iat(toEpochSecond(accessToken.getCreatedAt()))
                .sub(accessToken.getUser() != null ? accessToken.getUser().getId() : null)
                .build();
    }

    private long calculateExpiresIn(Instant expiresAt) {
        if (expiresAt == null) {
            return 0;
        }
        long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(0, seconds);
    }

    private String joinScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return "";
        }
        return String.join(" ", scopes);
    }

    private Long toEpochSecond(Instant instant) {
        return instant != null ? instant.getEpochSecond() : null;
    }
}
