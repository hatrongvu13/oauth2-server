package com.htv.oauth2.security;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.exception.security.UnauthorizedException;
import com.htv.oauth2.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequestScoped
public class UserContextProvider {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    UserRepository userRepository;

    /**
     * Get current authenticated user
     */
    public Optional<User> getCurrentUser() {
        if (securityIdentity.isAnonymous()) {
            return Optional.empty();
        }

        String userId = securityIdentity.getPrincipal().getName();
        return userRepository.findByIdOptional(userId);
    }

    /**
     * Get current user or throw exception
     */
    public User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException(
                        "Authentication required"
                ));
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        if (securityIdentity.isAnonymous()) {
            return null;
        }
        return securityIdentity.getPrincipal().getName();
    }

    /**
     * Check if user has role
     */
    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
