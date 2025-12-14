package com.htv.oauth2.repository;

import com.htv.oauth2.domain.AuthorizationCode;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AuthorizationCodeRepository implements PanacheRepositoryBase<AuthorizationCode, String> {

    public Optional<AuthorizationCode> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public Optional<AuthorizationCode> findValidCode(String code) {
        return find("code = ?1 and used = false and expiresAt > ?2",
                code, Instant.now())
                .firstResultOptional();
    }

    public void markAsUsed(String code) {
        update("used = true where code = ?1", code);
    }

    public long deleteExpired() {
        return delete("expiresAt < ?1", Instant.now());
    }

    public long deleteByUserId(String userId) {
        return delete("user.id", userId);
    }

    public List<AuthorizationCode> findByUserId(String userId) {
        return list("user.id", userId);
    }

    public List<AuthorizationCode> findByClientId(String clientId) {
        return list("client.clientId", clientId);
    }
}
