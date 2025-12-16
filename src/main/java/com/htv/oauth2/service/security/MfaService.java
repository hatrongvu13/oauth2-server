package com.htv.oauth2.service.security;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.exception.InvalidTokenException;
import com.htv.oauth2.service.CacheService;
import com.htv.oauth2.util.CryptoUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@ApplicationScoped
public class MfaService {

    @Inject
    CacheService cacheService;

    private static final int MFA_CODE_LENGTH = 6;
    private static final int MFA_TIME_STEP = 30; // seconds
    private static final int MFA_SESSION_VALIDITY = 300; // 5 minutes

    /**
     * Generate MFA session token (temporary token for MFA flow)
     */
    public String generateMfaSessionToken(String userId) {
        String token = CryptoUtil.generateSecureToken(32);
        String key = "mfa:session:" + token;

        // Cache user ID with token
        cacheService.put(key, userId, MFA_SESSION_VALIDITY);

        return token;
    }

    /**
     * Validate MFA session token and return user ID
     */
    public String validateMfaSessionToken(String token) {
        String key = "mfa:session:" + token;

        return cacheService.get(key)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired MFA session"));
    }

    /**
     * Verify TOTP MFA code
     */
    public boolean verifyMfaCode(User user, String code) {
        if (user.getMfaSecret() == null || user.getMfaSecret().isEmpty()) {
            throw new IllegalStateException("MFA not configured for user");
        }

        try {
            // Get current time slot
            long currentTimeSlot = Instant.now().getEpochSecond() / MFA_TIME_STEP;

            // Check current time slot and +/- 1 slot for clock skew
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTotpCode(user.getMfaSecret(), currentTimeSlot + i);
                if (code.equals(expectedCode)) {
                    // Check if this code was already used (prevent replay)
                    String usedKey = "mfa:used:" + user.getId() + ":" + code;
                    if (cacheService.exists(usedKey)) {
                        log.warn("MFA code already used: {}", code);
                        return false;
                    }

                    // Mark code as used
                    cacheService.put(usedKey, "true", MFA_TIME_STEP * 2);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Failed to verify MFA code", e);
            return false;
        }
    }

    /**
     * Generate TOTP code for given time slot
     */
    private String generateTotpCode(String secret, long timeSlot)
            throws NoSuchAlgorithmException, InvalidKeyException {

        // Decode base64 secret
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        // Convert time slot to bytes
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timeSlot);
        byte[] timeBytes = buffer.array();

        // Calculate HMAC-SHA1
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
        byte[] hash = mac.doFinal(timeBytes);

        // Extract dynamic binary code
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        // Generate 6-digit code
        int code = binary % 1000000;
        return String.format("%06d", code);
    }

    /**
     * Generate MFA secret for new user
     */
    public String generateMfaSecret() {
        return CryptoUtil.generateTotpSecret();
    }

    /**
     * Generate QR code URL for MFA setup
     */
    public String generateQrCodeUrl(String username, String secret, String issuer) throws UnsupportedEncodingException {
        // 1. Mã hóa Secret Key
        String encodedSecret = URLEncoder.encode(secret, StandardCharsets.UTF_8.toString());

        // 2. Mã hóa Issuer (đề phòng)
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8.toString());

        String totpUri = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encodedIssuer,
                username,
                encodedSecret, // Sử dụng encodedSecret
                encodedIssuer
        );
        return totpUri;
    }

    /**
     * Generate backup codes for MFA
     */
    public String[] generateBackupCodes(int count) {
        String[] codes = new String[count];
        for (int i = 0; i < count; i++) {
            codes[i] = CryptoUtil.generateAlphanumeric(8);
        }
        return codes;
    }
}