package com.htv.oauth2.repository;

import com.htv.oauth2.domain.AccessToken;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AccessTokenRepository implements PanacheRepositoryBase<AccessToken, String> {

    public Optional<AccessToken> findByToken(String token) {
        return find("token", token).firstResultOptional();
    }

    public Optional<AccessToken> findValidToken(String token) {
        return find("token = ?1 and revoked = false and expiresAt > ?2",
                token, Instant.now())
                .firstResultOptional();
    }

    public List<AccessToken> findByUserId(String userId) {
        return list("user.id", userId);
    }

    public List<AccessToken> findByClientId(String clientId) {
        return list("clientId", clientId);
    }

    public List<AccessToken> findValidByUserId(String userId) {
        return list("user.id = ?1 and revoked = false and expiresAt > ?2",
                userId, Instant.now());
    }

    public void revokeToken(String token) {
        update("revoked = true where token = ?1", token);
    }

    public void revokeAllByUserId(String userId) {
        update("revoked = true where user.id = ?1", userId);
    }

    public void revokeAllByClientId(String clientId) {
        update("revoked = true where clientId = ?1", clientId);
    }

    public long deleteExpired() {
        return delete("expiresAt < ?1 or revoked = true", Instant.now());
    }

    public long countValidTokensByUserId(String userId) {
        return count("user.id = ?1 and revoked = false and expiresAt > ?2",
                userId, Instant.now());
    }
}
