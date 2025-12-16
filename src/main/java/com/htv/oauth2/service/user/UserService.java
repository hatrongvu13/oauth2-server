package com.htv.oauth2.service.user;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.RegisterRequest;
import com.htv.oauth2.dto.request.UserUpdateRequest;
import com.htv.oauth2.dto.response.RegisterResponse;
import com.htv.oauth2.dto.response.UserResponse;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.mapper.UserMapper;
import com.htv.oauth2.repository.UserRepository;
import com.htv.oauth2.service.security.MfaService;
import com.htv.oauth2.service.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
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

    private static final String MFA_ISSUER_NAME = "HTV";

    /**
     * Register new user
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) throws UnsupportedEncodingException {
        log.info("Registering new user: {}", request.getUsername());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 1 Create user entity
        User user = userMapper.fromRegisterRequest(request);
        user.setPassword(passwordService.hashPassword(request.getPassword()));
        user.setRoles(Set.of("USER")); // Default role
        user.setMfaEnabled(false);
        user.setEmailVerified(true); // giả lập đã xác thực email

        // 2. TẠO VÀ LƯU SECRET KEY TẠM THỜI CHO MFA SETUP
        String secretKey = mfaService.generateMfaSecret();
        user.setMfaSecret(secretKey);

        userRepository.persist(user);

        // 3. Chuẩn bị dữ liệu phản hồi MFA
        String qrCodeUrl = mfaService.generateQrCodeUrl(
                user.getEmail(),
                secretKey,
                MFA_ISSUER_NAME
        );

        log.info("User registered successfully: {}", user.getId());
        // 4. Trả về RegisterResponse
        return RegisterResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .message("User registered successfully. MFA setup is required.")
                .mfaRequiredSetup(true)
                .mfaSecretKey(secretKey)
                .mfaQrCodeUrl(qrCodeUrl)
                .build();
    }

    /**
     * Find user by ID
     */
    public UserResponse findById(String userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponse(user);
    }

    /**
     * ENABLE MFA for the user (after verification)
     */
    @Transactional
    public void enableMfa(User user) {
        // Chỉ cần cập nhật trạng thái kích hoạt, vì Secret Key đã được lưu trước đó
        user.setMfaEnabled(true);
        userRepository.persist(user);
        log.info("MFA successfully enabled for user {}", user.getId());
    }

    /**
     * Find user by username
     */
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return userMapper.toResponse(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if email is being changed and already exists
        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        userMapper.updateUserFromRequest(request, user);
        userRepository.persist(user);

        log.info("User updated successfully: {}", userId);
        return userMapper.toResponse(user);
    }

    /**
     * Enable/disable user
     */
    @Transactional
    public void setUserEnabled(String userId, boolean enabled) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setEnabled(enabled);
        userRepository.persist(user);
        log.info("User {} set to enabled={}", userId, enabled);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.deleteById(userId)) {
            throw new UserNotFoundException(userId);
        }
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
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.getRoles().add(role);
        userRepository.persist(user);
        log.info("Role {} added to user {}", role, userId);
    }

    /**
     * Remove role from user
     */
    @Transactional
    public void removeRole(String userId, String role) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.getRoles().remove(role);
        userRepository.persist(user);
        log.info("Role {} removed from user {}", role, userId);
    }

    /**
     * Handle failed login attempt
     */
    @Transactional
    public void handleFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();

            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(Instant.now().plusSeconds(900)); // 15 minutes
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
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.resetFailedLoginAttempts();
        user.setLastLogin(Instant.now());
        userRepository.persist(user);
    }
}