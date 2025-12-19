package com.htv.oauth2.service.mfa;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.htv.oauth2.domain.MfaConfig;
import com.htv.oauth2.exception.CacheException;
import com.htv.oauth2.exception.ExpiredMfaTokenException;
import com.htv.oauth2.exception.ResourceNotFoundException;
import com.htv.oauth2.repository.MfaConfigRepository;
import com.htv.oauth2.service.cache.CacheService;
import com.htv.oauth2.util.CryptoUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@ApplicationScoped
public class MfaService {

    @Inject
    MfaConfigRepository mfaConfigRepository;

    @Inject
    CacheService cacheService;

    @ConfigProperty(name = "quarkus.smallrye-jwt.issuer", defaultValue = "HTV_OAuth2")
    String issuer;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int MFA_TIME_STEP = 30; // seconds
    private static final int BACKUP_CODE_COUNT = 10;

    /**
     * Generate MFA secret for user (setup flow)
     */
    @Transactional
    public MfaConfig generateMfaSecret(String userId, String username, String email) {
        MfaConfig existing = mfaConfigRepository.findByUserId(userId);
        if (existing != null && existing.getEnabled()) {
            throw new IllegalStateException("MFA already enabled for this user");
        }

        GoogleAuthenticatorKey credentials = gAuth.createCredentials();
        String secretKey = credentials.getKey();

        List<String> backupCodes = generateBackupCodes();

        MfaConfig config = existing != null ? existing : new MfaConfig();
        config.setUserId(userId);
        config.setUsername(username);
        config.setEmail(email);
        config.setSecretKey(secretKey);
        config.setEnabled(false); // Chưa enable cho đến khi verify code đầu tiên
        config.setBackupCodes(String.join(",", backupCodes));
        config.setVerifiedAt(null);

        if (existing == null) {
            mfaConfigRepository.persist(config);
        }

        log.info("Generated MFA secret for user: {}", userId);
        return config;
    }

    /**
     * Generate QR code as full data URI (data:image/png;base64,...)
     */
    public String generateQrCode(String username, String secretKey) {
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, username, new GoogleAuthenticatorKey.Builder(secretKey).build());

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code", e);
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    /**
     * Generate QR code as byte[]
     */
    public byte[] generateQrCodeImage(String username, String secretKey) {
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, username, new GoogleAuthenticatorKey.Builder(secretKey).build());

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code", e);
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    /**
     * Verify TOTP code and enable MFA (setup flow)
     */
    @Transactional
    public boolean verifyAndEnableMfa(String userId, int code) {
        MfaConfig config = getMfaConfigOrThrow(userId);

        boolean valid = gAuth.authorize(config.getSecretKey(), code);

        if (valid && !config.getEnabled()) {
            config.setEnabled(true);
            config.setVerifiedAt(LocalDateTime.now());
            log.info("MFA enabled for user: {}", userId);
        }
        return valid;
    }

    /**
     * Verify TOTP code during login (with anti-replay protection)
     */
    public boolean verifyMfaCodeDuringLogin(String userId, String codeStr) {
        int code;
        try {
            code = Integer.parseInt(codeStr);
        } catch (NumberFormatException e) {
            return false;
        }

        MfaConfig config = getMfaConfigOrThrow(userId);
        if (!config.getEnabled()) {
            return false;
        }

        // Anti-replay: đánh dấu code đã dùng trong 60 giây (2 time steps)
        String usedKey = "mfa:used:" + userId + ":" + code;
        if (cacheService.exists(usedKey)) {
            log.warn("Replay attack detected: MFA code {} already used for user {}", code, userId);
            return false;
        }

        boolean valid = gAuth.authorize(config.getSecretKey(), code);

        if (valid) {
            cacheService.put(usedKey, "true", MFA_TIME_STEP * 2);
            log.info("MFA code verified successfully for user: {}", userId);
        } else {
            log.warn("Invalid MFA code for user: {}", userId);
        }

        return valid;
    }

    /**
     * Verify backup code (and remove after use)
     */
    @Transactional
    public boolean verifyBackupCode(String userId, String backupCode) {
        MfaConfig config = getMfaConfigOrThrow(userId);
        if (!config.getEnabled()) {
            return false;
        }

        List<String> codes = List.of(config.getBackupCodes().split(","));
        if (codes.contains(backupCode.trim())) {
            List<String> remainingCodes = codes.stream()
                    .filter(c -> !c.equals(backupCode.trim()))
                    .collect(Collectors.toList());

            config.setBackupCodes(String.join(",", remainingCodes));
            log.info("Backup code used for user: {}", userId);
            return true;
        }
        return false;
    }

    /**
     * Check if MFA is enabled for user
     */
    public boolean isMfaEnabled(String userId) {
        MfaConfig config = mfaConfigRepository.findByUserId(userId);
        return config != null && config.getEnabled();
    }

    /**
     * Disable MFA
     */
    @Transactional
    public void disableMfa(String userId) {
        MfaConfig config = mfaConfigRepository.findByUserId(userId);
        if (config != null) {
            mfaConfigRepository.delete(config);
            log.info("MFA disabled for user: {}", userId);
        }
    }

    /**
     * Generate new backup codes (optional - regenerate if needed)
     */
    @Transactional
    public List<String> regenerateBackupCodes(String userId) {
        MfaConfig config = getMfaConfigOrThrow(userId);
        List<String> newCodes = generateBackupCodes();
        config.setBackupCodes(String.join(",", newCodes));
        return newCodes;
    }

    /**
     * Generate Mfa Token expire
     *
     * @param userId userId
     * @return key mfa token expire
     */

    public String generateMfaTokenExpire(String userId) {
        String keyMfaTokenExpire = CryptoUtil.generateSecureToken(40);
        cacheService.put(keyMfaTokenExpire, userId, 120); // expire 120s
        return keyMfaTokenExpire;
    }


    public String getUserIdFromMfaToken(String mfaToken) {
        if (cacheService.exists(mfaToken)) {
            return cacheService.get(mfaToken)
                    .orElseThrow(() -> new CacheException("Error when get user info from mfa token. Please try again!"));
        }
        throw new ExpiredMfaTokenException("Mfa Token is expired. Please login again!");
    }

    private MfaConfig getMfaConfigOrThrow(String userId) {
        MfaConfig mfaConfig = mfaConfigRepository.findByUserId(userId);
        if (Objects.isNull(mfaConfig)) {
            throw new ResourceNotFoundException(MfaConfig.class.getSimpleName(), "Not found with user " + userId);
        }
        return mfaConfig;
    }

    private List<String> generateBackupCodes() {
        return IntStream.range(0, BACKUP_CODE_COUNT)
                .mapToObj(i -> generateSingleBackupCode())
                .collect(Collectors.toList());
    }

    private String generateSingleBackupCode() {
        // Generate random 8-character code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = secureRandom.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        // Format as XXXX-XXXX for readability
        return code.substring(0, 4) + "-" + code.substring(4, 8);
    }
}