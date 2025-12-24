package com.htv.oauth2.service.user;

import com.htv.oauth2.domain.MfaConfig;
import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.mfa.EnableMfaRequest;
import com.htv.oauth2.dto.request.user.RegisterRequest;
import com.htv.oauth2.dto.request.user.UserUpdateRequest;
import com.htv.oauth2.dto.response.RegisterResponse;
import com.htv.oauth2.dto.response.UserResponse;
import com.htv.oauth2.exception.ApplicationException;
import com.htv.oauth2.exception.ErrorCode;
import com.htv.oauth2.mapper.UserMapper;
import com.htv.oauth2.repository.UserRepository;
import com.htv.oauth2.service.email.EmailService;
import com.htv.oauth2.service.mfa.MfaService;
import com.htv.oauth2.service.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    PasswordService passwordService;

    @Inject
    MfaService mfaService;

    @Inject
    EmailService emailService;

    /**
     * Register new user
     * - Tạo user bình thường
     * - Tạo MfaConfig riêng biệt (chưa enable)
     * - Trả về QR code + secret để setup MFA ngay sau đăng ký (tùy chọn)
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApplicationException(ErrorCode.PASSWORD_MISMATCH);
        }

        // Check duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApplicationException(ErrorCode.USERNAME_ALREADY_EXISTS, request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS, request.getEmail());
        }

        // Create and persist user
        User user = userMapper.fromRegisterRequest(request);
        user.setPassword(passwordService.hashPassword(request.getPassword()));
        user.setRoles(Set.of("USER"));
        user.setMfaEnabled(false);           // Sẽ cập nhật khi verify MFA thành công
        user.setEmailVerified(true);         // Giả lập đã verify email
        userRepository.persist(user);


        MfaConfig mfaConfig = mfaService.generateMfaSecret(user.getId(), request.getUsername(), request.getEmail());

        String qrCodeBase64 = mfaService.generateQrCode(request.getEmail(), mfaConfig.getSecretKey());
        log.info("Start send email enable Mfa : {}", qrCodeBase64);
        byte[] qrCodeBytes = mfaService.generateQrCodeImage(request.getEmail(), mfaConfig.getSecretKey());
        emailService.sendMfaSetupEmail(user.getEmail(), user.getUsername(), qrCodeBytes, mfaConfig.getSecretKey());

        log.info("User registered successfully: {}", user.getId());

        return RegisterResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .message("User registered successfully. Please set up MFA for enhanced security.")
                .mfaRequiredSetup(true)                  // Frontend có thể quyết định bắt buộc hay không
                .mfaSecretKey(mfaConfig.getSecretKey())
                .mfaQrCodeUrl(qrCodeBase64)               // Đã có prefix data:image/png;base64,
                .build();
    }

    /**
     * Find user by ID
     */
    public UserResponse findById(String userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));
        return userMapper.toResponse(user);
    }

    /**
     * ENABLE MFA for the user after verifying the first TOTP code
     */
    @Transactional
    public void enableMfa(String userId, EnableMfaRequest request) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, "User not found with ID: " + userId));

        if (user.getMfaEnabled()) {
            throw new ApplicationException(ErrorCode.INVALID_OPERATION, "MFA is already enabled for this user.");
        }

        // Verify code và tự động enable trong MfaService
        boolean verified = mfaService.verifyAndEnableMfa(userId, Integer.parseInt(request.getVerificationCode()));

        if (!verified) {
            throw new ApplicationException(ErrorCode.INVALID_MFA_CODE, "Invalid MFA verification code.");
        }

        // Cập nhật flag trong User entity (để dễ query trong login flow)
        user.setMfaEnabled(true);
        userRepository.persist(user);

        log.info("MFA successfully enabled for user {}", userId);
    }

    /**
     * DISABLE MFA for the user
     * - Xóa cấu hình MFA trong database
     * - Cập nhật trạng thái mfaEnabled của User entity về false
     */
    @Transactional
    public void disableMfa(String userId) {
        log.info("Disabling MFA for user: {}", userId);

        // 1. Tìm user để đảm bảo user tồn tại
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, "User not found with ID: " + userId));

        // 2. Xóa cấu hình MFA (Secret key, Backup codes) thông qua MfaService
        mfaService.disableMfa(userId);

        // 3. Cập nhật flag mfaEnabled trong bảng User
        user.setMfaEnabled(false);
        userRepository.persist(user);

        log.info("MFA has been successfully disabled for user {}", userId);
    }

    /**
     * Find user by username
     */
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, username));
        return userMapper.toResponse(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));

        // Check email conflict
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS, request.getEmail());
        }

        userMapper.updateUserFromRequest(request, user);
        userRepository.persist(user);

        log.info("User updated successfully: {}", userId);
        return userMapper.toResponse(user);
    }

    /**
     * Enable/disable user account
     */
    @Transactional
    public void setUserEnabled(String userId, boolean enabled) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));
        user.setEnabled(enabled);
        userRepository.persist(user);
        log.info("User {} set to enabled={}", userId, enabled);
    }

    /**
     * Delete user (cũng nên xóa MfaConfig nếu có)
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));

        // Xóa MFA config liên quan
        mfaService.disableMfa(userId);

        userRepository.delete(user);
        log.info("User deleted: {}", userId);
    }

    /**
     * List all users (admin)
     */
    public List<UserResponse> listAllUsers() {
        List<User> users = userRepository.listAll();
        return userMapper.toResponseList(users);
    }

    /**
     * Add role to user
     */
    @Transactional
    public void addRole(String userId, String role) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));
        user.getRoles().add(role.toUpperCase());
        userRepository.persist(user);
        log.info("Role {} added to user {}", role, userId);
    }

    /**
     * Remove role from user
     */
    @Transactional
    public void removeRole(String userId, String role) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));
        user.getRoles().remove(role.toUpperCase());
        userRepository.persist(user);
        log.info("Role {} removed from user {}", role, userId);
    }

    /**
     * Handle failed login attempt (brute-force protection)
     */
    @Transactional
    public void handleFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();

            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(Instant.now().plusSeconds(900)); // 15 minutes lock
                log.warn("Account locked due to failed login attempts: {}", username);
            }
            userRepository.persist(user);
        });
    }

    /**
     * Handle successful login
     */
    @Transactional
    public void handleSuccessfulLogin(String userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));
        user.resetFailedLoginAttempts();
        user.setLastLogin(Instant.now());
        userRepository.persist(user);
    }

    /**
     * Check if MFA is enabled for a user (dùng trong login flow)
     */
    public boolean isMfaEnabled(String userId) {
        return mfaService.isMfaEnabled(userId);
    }
}