package com.htv.oauth2.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
@ApplicationScoped
public class JwtConfig {

    @ConfigProperty(name = "quarkus.smallrye-jwt.issuer")
    String issuer;

    // ✅ Đổi thành property mà SmallRye JWT thực sự sử dụng
    @ConfigProperty(name = "smallrye.jwt.sign.key.location", defaultValue = "keys/private_key.pem")
    String privateKeyLocation;

    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "keys/public_key.pem")
    String publicKeyLocation;

    @ConfigProperty(name = "oauth2.jwt.access-token-expiry", defaultValue = "3600")
    Long accessTokenExpiry;

    @ConfigProperty(name = "oauth2.jwt.refresh-token-expiry", defaultValue = "86400")
    Long refreshTokenExpiry;

    void onStart(@Observes StartupEvent ev) {
        log.info("=== JWT Configuration ===");
        log.info("Issuer: {}", issuer);
        log.info("Sign Key Location: {}", privateKeyLocation);
        log.info("Verify Key Location: {}", publicKeyLocation);
        log.info("Access Token Expiry: {}s", accessTokenExpiry);
        log.info("Refresh Token Expiry: {}s", refreshTokenExpiry);

        // Verify keys exist and are valid
        verifyKeyExists(privateKeyLocation, "Private Key");
        verifyKeyExists(publicKeyLocation, "Public Key");

        log.info("=== JWT Configuration Verified ===");
    }

    private void verifyKeyExists(String location, String keyType) {
        try {
            log.info("Verifying {}: {}", keyType, location);

            // Remove prefixes
            String path = location.replace("classpath:", "")
                    .replace("file:", "");

            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            log.info("Resolved path: {}", path);

            // Try to load
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream(path);
            }

            if (is == null) {
                log.error("❌ {} NOT FOUND at: {} (resolved: {})", keyType, location, path);
                throw new IllegalStateException(keyType + " not found: " + location);
            }

            // Read content
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            log.info("✅ {} found - {} bytes", keyType, content.length());
            log.info("First 30 chars: {}", content.substring(0, Math.min(30, content.length())));

            // Verify format
            if (keyType.equals("Private Key")) {
                if (content.contains("BEGIN PRIVATE KEY")) {
                    log.info("✅ Private key format: PKCS#8 (correct)");
                } else if (content.contains("BEGIN RSA PRIVATE KEY")) {
                    log.error("❌ Private key format: PKCS#1 (incorrect - need PKCS#8)");
                    throw new IllegalStateException("Private key must be in PKCS#8 format. Use: openssl genpkey ...");
                } else {
                    log.error("❌ Invalid private key format");
                    throw new IllegalStateException("Invalid private key format");
                }
            } else {
                if (!content.contains("BEGIN PUBLIC KEY")) {
                    log.error("❌ Invalid public key format");
                    throw new IllegalStateException("Invalid public key format");
                }
                log.info("✅ Public key format valid");
            }

        } catch (Exception e) {
            log.error("❌ Failed to verify {}: {}", keyType, e.getMessage(), e);
            throw new RuntimeException("Key verification failed for " + keyType, e);
        }
    }
}