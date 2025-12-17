package com.htv.oauth2.util;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@ApplicationScoped
public class CryptoUtil {

    // Lazy initialization for GraalVM Native Image compatibility
    private static final class SecureRandomHolder {
        static final SecureRandom INSTANCE = new SecureRandom();
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Get SecureRandom instance (lazy-initialized)
     */
    private static SecureRandom getSecureRandom() {
        return SecureRandomHolder.INSTANCE;
    }

    /**
     * Generate secure random token
     */
    public static String generateSecureToken(int length) {
        byte[] randomBytes = new byte[length];
        getSecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generate authorization code
     */
    public static String generateAuthorizationCode() {
        return generateSecureToken(32);
    }

    /**
     * Generate client ID
     */
    public static String generateClientId() {
        return "client_" + generateSecureToken(16);
    }

    /**
     * Generate client secret
     */
    public static String generateClientSecret() {
        return generateSecureToken(32);
    }

    /**
     * Generate random alphanumeric string
     */
    public static String generateAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        SecureRandom random = getSecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * Hash data using SHA-256
     */
    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verify PKCE code challenge
     */
    public static boolean verifyPkceChallenge(String codeVerifier, String codeChallenge, String method) {
        if ("plain".equals(method)) {
            return codeVerifier.equals(codeChallenge);
        } else if ("S256".equals(method)) {
            String computed = sha256(codeVerifier);
            return computed.equals(codeChallenge);
        }
        return false;
    }

}
