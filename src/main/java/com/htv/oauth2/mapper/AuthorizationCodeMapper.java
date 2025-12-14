package com.htv.oauth2.mapper;

import com.htv.oauth2.domain.AuthorizationCode;
import com.htv.oauth2.dto.response.AuthorizationResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.Instant; /**
 * Manual implementation of AuthorizationCodeMapper
 */
@ApplicationScoped
public class AuthorizationCodeMapper {

    public AuthorizationResponse toResponse(AuthorizationCode authCode, String state) {
        if (authCode == null) return null;

        return AuthorizationResponse.builder()
                .code(authCode.getCode())
                .state(state)
                .redirectUri(authCode.getRedirectUri())
                .expiresIn(calculateExpiresIn(authCode.getExpiresAt()))
                .build();
    }

    private long calculateExpiresIn(Instant expiresAt) {
        if (expiresAt == null) {
            return 0;
        }
        long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(0, seconds);
    }
}
