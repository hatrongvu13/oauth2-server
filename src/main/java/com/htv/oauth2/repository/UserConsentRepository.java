package com.htv.oauth2.repository;

import com.htv.oauth2.domain.UserConsent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserConsentRepository implements PanacheRepositoryBase<UserConsent, String> {

    public Optional<UserConsent> findByUserAndClient(String userId, String clientId) {
        return find("user.id = ?1 and client.clientId = ?2", userId, clientId)
                .firstResultOptional();
    }

    public List<UserConsent> findByUserId(String userId) {
        return list("user.id", userId);
    }

    public List<UserConsent> findByClientId(String clientId) {
        return list("client.clientId", clientId);
    }

    public void deleteByUserAndClient(String userId, String clientId) {
        delete("user.id = ?1 and client.clientId = ?2", userId, clientId);
    }

    public void deleteByUserId(String userId) {
        delete("user.id", userId);
    }

    public void deleteByClientId(String clientId) {
        delete("client.clientId", clientId);
    }

    public boolean hasConsent(String userId, String clientId) {
        return count("user.id = ?1 and client.clientId = ?2", userId, clientId) > 0;
    }
}
