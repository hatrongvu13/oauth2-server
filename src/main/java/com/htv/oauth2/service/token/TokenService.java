package com.htv.oauth2.service.token;

import com.htv.oauth2.domain.*;
import com.htv.oauth2.dto.response.TokenIntrospectionResponse;
import com.htv.oauth2.dto.response.TokenResponse;
import com.htv.oauth2.exception.auth.oauth2.InvalidClientException;
import com.htv.oauth2.exception.auth.token.ExpiredTokenException;
import com.htv.oauth2.exception.auth.token.InvalidTokenException;
import com.htv.oauth2.exception.auth.token.TokenRevokedException;
import com.htv.oauth2.mapper.TokenMapper;
import com.htv.oauth2.repository.*;
import com.htv.oauth2.util.CryptoUtil;
import com.htv.oauth2.util.DateTimeUtil;
import com.htv.oauth2.util.JwtUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

// ============================================
// Token Service
// ============================================

@Slf4j
@ApplicationScoped
public class TokenService {

    @Inject
    AccessTokenRepository accessTokenRepository;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @Inject
    JwtUtil jwtUtil;

    @Inject
    TokenMapper tokenMapper;

    /**
     * Generate access and refresh tokens
     */
    @Transactional
    public TokenResponse generateTokens(User user, Client client, Set<String> scopes) {
        log.info("Generating tokens for user {} and client {}", user.getId(), client.getClientId());

        // Generate JWT access token
        String accessTokenValue = jwtUtil.generateAccessToken(
                user.getId(),
                client.getClientId(),
                scopes
        );

        // Create access token entity
        AccessToken accessToken = AccessToken.builder()
                .token(accessTokenValue)
                .clientId(client.getClientId())
                .user(user)
                .scopes(scopes)
                .expiresAt(DateTimeUtil.expiresAt(client.getAccessTokenValidity()))
                .build();

        accessTokenRepository.persist(accessToken);

        // Generate refresh token
        String refreshTokenValue = CryptoUtil.generateSecureToken(64);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .accessToken(accessToken)
                .clientId(client.getClientId())
                .user(user)
                .expiresAt(DateTimeUtil.expiresAt(client.getRefreshTokenValidity()))
                .build();

        refreshTokenRepository.persist(refreshToken);

        // Build response
        return TokenResponse.builder()
                .accessToken(accessTokenValue)
                .tokenType("Bearer")
                .expiresIn(client.getAccessTokenValidity().longValue())
                .refreshToken(refreshTokenValue)
                .refreshExpiresIn(client.getRefreshTokenValidity().longValue())
                .scope(String.join(" ", scopes))
                .build();
    }

    /**
     * Refresh access token
     */
    @Transactional
    public TokenResponse refreshToken(String refreshTokenValue, Client client) {
        log.info("Refreshing token for client {}", client.getClientId());

        // Find and validate refresh token
        RefreshToken refreshToken = refreshTokenRepository.findValidToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        // Validate client
        if (!refreshToken.getClientId().equals(client.getClientId())) {
            throw new InvalidClientException("Refresh token does not belong to this client");
        }

        // Revoke old tokens
        accessTokenRepository.revokeToken(refreshToken.getAccessToken().getToken());
        refreshTokenRepository.revokeToken(refreshTokenValue);

        // Generate new tokens
        return generateTokens(
                refreshToken.getUser(),
                client,
                refreshToken.getAccessToken().getScopes()
        );
    }

    /**
     * Validate access token
     */
    public AccessToken validateToken(String tokenValue) {
        AccessToken token = accessTokenRepository.findValidToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired access token"));

        if (!token.isValid()) {
            if (token.getRevoked()) {
                throw new TokenRevokedException("Token has been revoked");
            } else {
                throw new ExpiredTokenException("Token has expired");
            }
        }

        return token;
    }

    /**
     * Introspect token
     */
    public TokenIntrospectionResponse introspectToken(String tokenValue) {
        return accessTokenRepository.findByToken(tokenValue)
                .map(token -> {
                    if (token.isValid()) {
                        return tokenMapper.toIntrospectionResponse(token);
                    } else {
                        return TokenIntrospectionResponse.builder()
                                .active(false)
                                .build();
                    }
                })
                .orElse(TokenIntrospectionResponse.builder()
                        .active(false)
                        .build());
    }

    /**
     * Revoke token
     */
    @Transactional
    public void revokeToken(String tokenValue) {
        log.info("Revoking token");

        // Try as access token first
        accessTokenRepository.findByToken(tokenValue)
                .ifPresentOrElse(
                        accessToken -> {
                            accessTokenRepository.revokeToken(tokenValue);
                            // Also revoke associated refresh token
                            refreshTokenRepository.revokeByAccessTokenId(accessToken.getId());
                        },
                        // Try as refresh token
                        () -> refreshTokenRepository.findByToken(tokenValue)
                                .ifPresent(refreshToken ->
                                        refreshTokenRepository.revokeToken(tokenValue)
                                )
                );
    }

    /**
     * Revoke all user tokens
     */
    @Transactional
    public void revokeAllUserTokens(String userId) {
        log.info("Revoking all tokens for user {}", userId);
        accessTokenRepository.revokeAllByUserId(userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Clean up expired tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");
        long deletedAccess = accessTokenRepository.deleteExpired();
        long deletedRefresh = refreshTokenRepository.deleteExpired();
        log.info("Deleted {} access tokens and {} refresh tokens", deletedAccess, deletedRefresh);
    }
}
