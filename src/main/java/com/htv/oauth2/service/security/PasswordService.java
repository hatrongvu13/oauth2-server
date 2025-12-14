package com.htv.oauth2.service.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

// ============================================
// Password Service
// ============================================

@Slf4j
@ApplicationScoped
public class PasswordService {

    /**
     * Hash password using BCrypt
     */
    public String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    /**
     * Verify password against hash
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }

    /**
     * Check if password needs rehashing (e.g., algorithm upgrade)
     */
    public boolean needsRehash(String hashedPassword) {
        // BCrypt format: $2a$rounds$salt+hash
        // Current recommended rounds: 10-12
        if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
            return true;
        }

        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) return true;

            int rounds = Integer.parseInt(parts[2]);
            return rounds < 10; // Rehash if using less than 10 rounds
        } catch (Exception e) {
            log.warn("Failed to parse BCrypt hash format", e);
            return true;
        }
    }
}