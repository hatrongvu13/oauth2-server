package com.htv.oauth2.repository;

import com.htv.oauth2.domain.Client;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClientRepository implements PanacheRepositoryBase<Client, String> {

    public Optional<Client> findByClientId(String clientId) {
        return find("clientId", clientId).firstResultOptional();
    }

    public boolean existsByClientId(String clientId) {
        return count("clientId", clientId) > 0;
    }

    public List<Client> findAllEnabled() {
        return list("enabled", true);
    }

    public List<Client> searchByName(String query, Page page) {
        return find("clientName LIKE ?1", "%" + query + "%")
                .page(page)
                .list();
    }

    public long countEnabled() {
        return count("enabled", true);
    }

    public Optional<Client> findByClientIdAndEnabled(String clientId, boolean enabled) {
        return find("clientId = ?1 and enabled = ?2", clientId, enabled)
                .firstResultOptional();
    }
}
