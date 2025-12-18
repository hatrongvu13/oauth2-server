//package com.htv.oauth2.service.mfa;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;
//import com.htv.oauth2.domain.User;
//import com.htv.oauth2.exception.InvalidTokenException;
//import com.htv.oauth2.service.CacheService;
//import com.htv.oauth2.util.CryptoUtil;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.ByteArrayOutputStream;
//import java.net.URLEncoder;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.time.Instant;
//import java.util.Base64;
//
//@Slf4j
//@ApplicationScoped
//public class MfaService {
//
//    @Inject
//    CacheService cacheService;
//
//    private static final int MFA_CODE_LENGTH = 6;
//    private static final int MFA_TIME_STEP = 30; // seconds
//    private static final int MFA_SESSION_VALIDITY = 300; // 5 minutes
//
//    // Base32 alphabet for encoding (RFC 4648)
//    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
//
//    /**
//     * Generate MFA secret in Base32 format (required by Google Authenticator)
//     */
//    public String generateMfaSecret() {
//        // Generate 20 random bytes (160 bits)
//        SecureRandom random = new SecureRandom();
//        byte[] bytes = new byte[20];
//        random.nextBytes(bytes);
//
//        // Encode to Base32
//        return encodeBase32(bytes);
//    }
//
//    /**
//     * Encode bytes to Base32 string (RFC 4648)
//     */
//    private String encodeBase32(byte[] bytes) {
//        StringBuilder result = new StringBuilder();
//        int buffer = 0;
//        int bitsLeft = 0;
//
//        for (byte b : bytes) {
//            buffer <<= 8;
//            buffer |= (b & 0xFF);
//            bitsLeft += 8;
//
//            while (bitsLeft >= 5) {
//                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
//                result.append(BASE32_CHARS.charAt(index));
//                bitsLeft -= 5;
//            }
//        }
//
//        if (bitsLeft > 0) {
//            int index = (buffer << (5 - bitsLeft)) & 0x1F;
//            result.append(BASE32_CHARS.charAt(index));
//        }
//
//        // Add padding to make length multiple of 8
//        while (result.length() % 8 != 0) {
//            result.append('=');
//        }
//
//        return result.toString();
//    }
//
//    /**
//     * Decode Base32 string to bytes
//     */
//    private byte[] decodeBase32(String encoded) {
//        // Remove padding
//        encoded = encoded.replaceAll("=", "");
//
//        ByteArrayOutputStream result = new ByteArrayOutputStream();
//        int buffer = 0;
//        int bitsLeft = 0;
//
//        for (char c : encoded.toCharArray()) {
//            int value = BASE32_CHARS.indexOf(c);
//            if (value == -1) {
//                throw new IllegalArgumentException("Invalid Base32 character: " + c);
//            }
//
//            buffer <<= 5;
//            buffer |= value;
//            bitsLeft += 5;
//
//            if (bitsLeft >= 8) {
//                result.write((buffer >> (bitsLeft - 8)) & 0xFF);
//                bitsLeft -= 8;
//            }
//        }
//
//        return result.toByteArray();
//    }
//
//    /**
//     * Generate QR code URL for MFA setup (otpauth format)
//     */
//    public String generateQrCodeUrl(String username, String secret, String issuer) {
//        try {
//            // Important: Secret must be Base32 encoded (already done in generateMfaSecret)
//            // Do NOT re-encode the secret
//
//            String totpUri = String.format(
//                    "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
//                    URLEncoder.encode(issuer, StandardCharsets.UTF_8),
//                    URLEncoder.encode(username, StandardCharsets.UTF_8),
//                    secret, // Already Base32 encoded, no need to URL encode
//                    URLEncoder.encode(issuer, StandardCharsets.UTF_8)
//            );
//
//            log.debug("Generated TOTP URI: {}", totpUri);
//            return totpUri;
//
//        } catch (Exception e) {
//            log.error("Error generating QR code URL", e);
//            throw new RuntimeException("Could not generate QR code URL", e);
//        }
//    }
//
//    /**
//     * Generate QR code as Base64 PNG image
//     */
//    public String generateQrCodeBase64(String qrCodeUrl) {
//        try {
//            int width = 300;
//            int height = 300;
//
//            QRCodeWriter qrCodeWriter = new QRCodeWriter();
//            BitMatrix bitMatrix = qrCodeWriter.encode(
//                    qrCodeUrl,
//                    BarcodeFormat.QR_CODE,
//                    width,
//                    height
//            );
//
//            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
//            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
//            byte[] pngData = pngOutputStream.toByteArray();
//
//            String base64Image = Base64.getEncoder().encodeToString(pngData);
//            log.debug("Generated QR code image, size: {} bytes", pngData.length);
//
//            return base64Image;
//
//        } catch (Exception e) {
//            log.error("Failed to generate QR Code image", e);
//            throw new RuntimeException("Could not generate QR code", e);
//        }
//    }
//
//    /**
//     * Verify TOTP MFA code
//     */
//    public boolean verifyMfaCode(User user, String code) {
//        if (user.getMfaSecret() == null || user.getMfaSecret().isEmpty()) {
//            throw new IllegalStateException("MFA not configured for user");
//        }
//
//        try {
//            // Get current time slot
//            long currentTimeSlot = Instant.now().getEpochSecond() / MFA_TIME_STEP;
//
//            // Check current time slot and +/- 1 slot for clock skew
//            for (int i = -1; i <= 1; i++) {
//                String expectedCode = generateTotpCode(user.getMfaSecret(), currentTimeSlot + i);
//                if (code.equals(expectedCode)) {
//                    // Check if this code was already used (prevent replay)
//                    String usedKey = "mfa:used:" + user.getId() + ":" + code;
//                    if (cacheService.exists(usedKey)) {
//                        log.warn("MFA code already used: {}", code);
//                        return false;
//                    }
//
//                    // Mark code as used
//                    cacheService.put(usedKey, "true", MFA_TIME_STEP * 2);
//                    log.info("MFA code verified successfully for user: {}", user.getId());
//                    return true;
//                }
//            }
//
//            log.warn("Invalid MFA code for user: {}", user.getId());
//            return false;
//
//        } catch (Exception e) {
//            log.error("Failed to verify MFA code", e);
//            return false;
//        }
//    }
//
//    /**
//     * Generate TOTP code for given time slot
//     */
//    private String generateTotpCode(String secret, long timeSlot)
//            throws NoSuchAlgorithmException, InvalidKeyException {
//
//        // Decode Base32 secret
//        byte[] keyBytes = decodeBase32(secret);
//
//        // Convert time slot to bytes (big-endian)
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.putLong(timeSlot);
//        byte[] timeBytes = buffer.array();
//
//        // Calculate HMAC-SHA1
//        Mac mac = Mac.getInstance("HmacSHA1");
//        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
//        byte[] hash = mac.doFinal(timeBytes);
//
//        // Extract dynamic binary code (Dynamic Truncation)
//        int offset = hash[hash.length - 1] & 0x0F;
//        int binary = ((hash[offset] & 0x7F) << 24) |
//                ((hash[offset + 1] & 0xFF) << 16) |
//                ((hash[offset + 2] & 0xFF) << 8) |
//                (hash[offset + 3] & 0xFF);
//
//        // Generate 6-digit code
//        int code = binary % 1000000;
//        return String.format("%06d", code);
//    }
//
//    /**
//     * Generate MFA session token (temporary token for MFA flow)
//     */
//    public String generateMfaSessionToken(String userId) {
//        String token = CryptoUtil.generateSecureToken(32);
//        String key = "mfa:session:" + token;
//
//        // Cache user ID with token
//        cacheService.put(key, userId, MFA_SESSION_VALIDITY);
//
//        return token;
//    }
//
//    /**
//     * Validate MFA session token and return user ID
//     */
//    public String validateMfaSessionToken(String token) {
//        String key = "mfa:session:" + token;
//
//        return cacheService.get(key)
//                .orElseThrow(() -> new InvalidTokenException("Invalid or expired MFA session"));
//    }
//
//    /**
//     * Generate backup codes for MFA
//     */
//    public String[] generateBackupCodes(int count) {
//        String[] codes = new String[count];
//        for (int i = 0; i < count; i++) {
//            codes[i] = CryptoUtil.generateAlphanumeric(8);
//        }
//        return codes;
//    }
//
//    /**
//     * Test method to verify secret generation and code verification
//     */
//    public void testMfaGeneration() {
//        try {
//            String secret = generateMfaSecret();
//            log.info("Generated secret (Base32): {}", secret);
//
//            long currentTimeSlot = Instant.now().getEpochSecond() / MFA_TIME_STEP;
//            String code = generateTotpCode(secret, currentTimeSlot);
//            log.info("Generated TOTP code: {}", code);
//
//            // Verify the code we just generated
//            User testUser = new User();
//            testUser.setMfaSecret(secret);
//            testUser.setId("test-user");
//
//            boolean valid = verifyMfaCode(testUser, code);
//            log.info("Code verification: {}", valid ? "SUCCESS" : "FAILED");
//
//        } catch (Exception e) {
//            log.error("MFA test failed", e);
//        }
//    }
//}
