package com.htv.oauth2.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,100}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    /**
     * Validate redirect URI
     */
    public static boolean isValidRedirectUri(String redirectUri) {
        if (!isValidUrl(redirectUri)) {
            return false;
        }
        // Additional checks: no fragments, HTTPS in production, etc.
        return !redirectUri.contains("#");
    }

    /**
     * Validate password strength
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Validate OAuth2 grant type
     */
    public static boolean isValidGrantType(String grantType) {
        return Set.of(
                "authorization_code",
                "refresh_token",
                "client_credentials",
                "password"
        ).contains(grantType);
    }

    /**
     * Validate OAuth2 response type
     */
    public static boolean isValidResponseType(String responseType) {
        return Set.of("code", "token").contains(responseType);
    }

    /**
     * Validate scope
     */
    public static boolean isValidScope(String scope) {
        if (scope == null || scope.trim().isEmpty()) {
            return false;
        }
        // Scopes should be alphanumeric with underscores/dots
        return scope.matches("^[a-zA-Z0-9_.\\s]+$");
    }

    /**
     * Validate that scopes are allowed for client
     */
    public static boolean areScopesAllowed(Set<String> requestedScopes, Set<String> allowedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            return true;
        }
        return allowedScopes != null && allowedScopes.containsAll(requestedScopes);
    }
}
