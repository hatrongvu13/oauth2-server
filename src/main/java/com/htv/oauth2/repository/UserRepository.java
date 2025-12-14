package com.htv.oauth2.repository;

import com.htv.oauth2.domain.*;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, String> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return find("username = ?1 or email = ?1", identifier).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public List<User> findAllEnabled() {
        return list("enabled", true);
    }

    public List<User> findByRole(String role) {
        return list("SELECT u FROM User u JOIN u.roles r WHERE r = ?1", role);
    }

    public List<User> searchByUsername(String query, Page page) {
        return find("username LIKE ?1", "%" + query + "%")
                .page(page)
                .list();
    }

    public long countEnabled() {
        return count("enabled", true);
    }

    public void updateLastLogin(String userId) {
        update("lastLogin = ?1, updatedAt = ?1 where id = ?2",
                Instant.now(), userId);
    }
}
