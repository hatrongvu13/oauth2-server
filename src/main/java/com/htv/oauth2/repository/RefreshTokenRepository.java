package com.htv.oauth2.repository;

import com.htv.oauth2.domain.RefreshToken;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepositoryBase<RefreshToken, String> {

    public Optional<RefreshToken> findByToken(String token) {
        return find("token", token).firstResultOptional();
    }

    public Optional<RefreshToken> findValidToken(String token) {
        return find("token = ?1 and revoked = false and expiresAt > ?2",
                token, Instant.now())
                .firstResultOptional();
    }

    public Optional<RefreshToken> findByAccessTokenId(String accessTokenId) {
        return find("accessToken.id", accessTokenId).firstResultOptional();
    }

    public List<RefreshToken> findByUserId(String userId) {
        return list("user.id", userId);
    }

    public List<RefreshToken> findByClientId(String clientId) {
        return list("clientId", clientId);
    }

    public void revokeToken(String token) {
        update("revoked = true where token = ?1", token);
    }

    public void revokeByAccessTokenId(String accessTokenId) {
        update("revoked = true where accessToken.id = ?1", accessTokenId);
    }

    public void revokeAllByUserId(String userId) {
        update("revoked = true where user.id = ?1", userId);
    }

    public long deleteExpired() {
        return delete("expiresAt < ?1 or revoked = true", Instant.now());
    }

    public long countValidByUserId(String userId) {
        return count("user.id = ?1 and revoked = false and expiresAt > ?2",
                userId, Instant.now());
    }
}
