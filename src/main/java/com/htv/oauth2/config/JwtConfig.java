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

    // Sửa: thêm .key vào path
    @ConfigProperty(name = "quarkus.smallrye-jwt.sign.key.location")
    String privateKeyLocation;

    @ConfigProperty(name = "quarkus.smallrye-jwt.verify.key.location")
    String publicKeyLocation;

    @ConfigProperty(name = "oauth2.jwt.access-token-expiry", defaultValue = "3600")
    Long accessTokenExpiry;

    @ConfigProperty(name = "oauth2.jwt.refresh-token-expiry", defaultValue = "86400")
    Long refreshTokenExpiry;

    void onStart(@Observes StartupEvent ev) {
        log.info("JWT Configuration:");
        log.info("  Issuer: {}", issuer);
        log.info("  Private Key: {}", privateKeyLocation);
        log.info("  Public Key: {}", publicKeyLocation);
        log.info("  Access Token Expiry: {}s", accessTokenExpiry);
        log.info("  Refresh Token Expiry: {}s", refreshTokenExpiry);

        // Verify keys exist
        verifyKeyExists(privateKeyLocation, "Private Key");
        verifyKeyExists(publicKeyLocation, "Public Key");
    }

    private void verifyKeyExists(String location, String keyType) {
        try {
            // Xử lý cả trường hợp có hoặc không có "classpath:" prefix
            String path = location.replace("classpath:", "")
                    .replace("file:", "");

            // Nếu path bắt đầu bằng /, bỏ nó đi
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                // Thử cách khác nếu không tìm thấy
                is = getClass().getClassLoader().getResourceAsStream(path);
            }

            if (is == null) {
                log.error("{} NOT FOUND at: {} (resolved path: {})", keyType, location, path);
                throw new IllegalStateException(keyType + " not found: " + location);
            }

            // Read and verify format
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            if (keyType.equals("Private Key")) {
                if (!content.contains("BEGIN PRIVATE KEY") &&
                        !content.contains("BEGIN RSA PRIVATE KEY")) {
                    throw new IllegalStateException("Invalid private key format");
                }
            } else {
                if (!content.contains("BEGIN PUBLIC KEY") &&
                        !content.contains("BEGIN RSA PUBLIC KEY")) {
                    throw new IllegalStateException("Invalid public key format");
                }
            }

            log.info("  ✓ {} verified: {} bytes", keyType, content.length());

        } catch (Exception e) {
            log.error("Failed to verify {}: {}", keyType, e.getMessage(), e);
            throw new RuntimeException("Key verification failed for " + keyType, e);
        }
    }
}