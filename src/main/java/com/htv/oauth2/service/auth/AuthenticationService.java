package com.htv.oauth2.service.auth;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.auth.LoginRequest;
import com.htv.oauth2.exception.ApplicationException;
import com.htv.oauth2.exception.ErrorCode;
import com.htv.oauth2.repository.UserRepository;
import com.htv.oauth2.service.AuditService;
import com.htv.oauth2.service.RateLimitService;
import com.htv.oauth2.service.mfa.MfaService;
import com.htv.oauth2.service.security.PasswordService;
import com.htv.oauth2.service.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AuthenticationService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    UserService userService;

    @Inject
    MfaService mfaService;

    @Inject
    AuditService auditService;

    @Inject
    RateLimitService rateLimitService;

    /**
     * Authenticate user with username and password
     * Returns authenticated user or throws exception
     */
    @Transactional
    public User authenticateUser(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Authenticating user: {} from IP: {}", request.getUsername(), ipAddress);

        // 1. Check rate limiting
        if (rateLimitService.checkLogin(ipAddress).isBlocked()) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            auditService.logAnonymous("LOGIN_RATE_LIMIT", ipAddress,
                    "FAILURE", ipAddress, userAgent);
            throw new ApplicationException(ErrorCode.RATE_LIMIT_EXCEEDED, "Too many login attempts. Please try again later.", 300L);
        }

        // 2. Find user
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> {
                    auditService.logAnonymous("LOGIN_FAILED", request.getUsername(),
                            "FAILURE", ipAddress, userAgent);
                    return new ApplicationException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
                });

        // 3. Check account status (Locked/Enabled/Verified)
        validateUserStatus(user, ipAddress, userAgent);

        // 4. Verify password
        if (!passwordService.verifyPassword(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            userService.handleFailedLogin(request.getUsername());
            auditService.logFailure(user, "LOGIN_FAILED", "invalid_password",
                    "Invalid password", ipAddress, userAgent);
            throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS,"Invalid username or password");
        }

        // 5. Check if password needs rehashing
        if (passwordService.needsRehash(user.getPassword())) {
            log.info("Rehashing password for user: {}", user.getId());
            user.setPassword(passwordService.hashPassword(request.getPassword()));
            userRepository.persist(user);
        }

        // 6. Handle MFA if enabled
        if (user.getMfaEnabled()) {
            handleMfaChallenge(user, request, ipAddress, userAgent);
        }

        // 7. Successful Login: Reset rate limit and update stats
        userService.handleSuccessfulLogin(user.getId());
        auditService.logSuccess(user, "LOGIN_SUCCESS", null, ipAddress, userAgent);

        log.info("User authenticated successfully: {}", user.getId());
        return user;
    }

    /**
     * Helper to validate status
     */
    private void validateUserStatus(User user, String ipAddress, String userAgent) {
        if (user.isAccountLocked()) {
            log.warn("Login attempt on locked account: {}", user.getUsername());
            auditService.logFailure(user, "LOGIN_FAILED", "account_locked",
                    "Account is locked", ipAddress, userAgent);
            throw new ApplicationException(ErrorCode.ACCOUNT_LOCKED, "Account is temporarily locked.");
        }

        if (!user.getEnabled()) {
            log.warn("Login attempt on disabled account: {}", user.getUsername());
            auditService.logFailure(user, "LOGIN_FAILED", "account_disabled",
                    "Account is disabled", ipAddress, userAgent);
            throw new ApplicationException(ErrorCode.ACCOUNT_DISABLED, "Account is disabled.");
        }

        if (!user.getEmailVerified()) {
            log.warn("Login attempt on unverified account: {}", user.getUsername());
            throw new ApplicationException(ErrorCode.EMAIL_NOT_VERIFIED, "Please verify your email address.");
        }
    }

    /**
     * Helper to handle MFA flow
     */
    private void handleMfaChallenge(User user, LoginRequest request, String ipAddress, String userAgent) {
        if (request.getMfaCode() == null) {
            throw new ApplicationException(ErrorCode.MFA_REQUIRED ,"MFA code required")
                    .withMfaToken(mfaService.generateMfaTokenExpire(user.getId()));
        }

        // Check Rate Limit cho MFA (Sử dụng RateLimitService mới)
        if (rateLimitService.checkMfa(user.getId()).isBlocked()) {
            log.warn("MFA rate limit exceeded for user: {}", user.getId());
            auditService.logFailure(user, "MFA_RATE_LIMIT", "too_many_attempts",
                    "MFA rate limit exceeded", ipAddress, userAgent);
            throw new ApplicationException(ErrorCode.RATE_LIMIT_EXCEEDED, "Too many MFA attempts. Please try again later.", 60L);
        }

        // Verify MFA code (Sử dụng method verifyMfaCodeDuringLogin của bạn)
        if (!mfaService.verifyMfaCodeDuringLogin(user.getId(), request.getMfaCode())) {
            log.warn("Invalid MFA code for user: {}", user.getId());
            auditService.logFailure(user, "MFA_FAILED", "invalid_code",
                    "Invalid MFA code", ipAddress, userAgent);
            throw new ApplicationException(ErrorCode.INVALID_MFA_CODE, "Invalid MFA code. Please try again.");
        }

        log.info("MFA verification successful for user: {}", user.getId());
    }

    @Transactional
    public User validateCredentials(String username, String password) {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials"));

        if (!user.getEnabled() || user.isAccountLocked()) {
            throw new ApplicationException(ErrorCode.ACCOUNT_LOCKED, "Account is unavailable");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS ,"Invalid credentials");
        }

        return user;
    }

    @Transactional
    public User verifyMfaSession(String userId, String mfaCode, String ipAddress, String userAgent) {
        // Kiểm tra rate limit cho MFA session
        if (rateLimitService.checkMfa(userId).isBlocked()) {
            throw new ApplicationException(ErrorCode.RATE_LIMIT_EXCEEDED ,"Too many MFA attempts.", 60L);
        }

        if (!mfaService.verifyMfaCodeDuringLogin(userId, mfaCode)) {
            User tempUser = userRepository.findByIdOptional(userId).orElse(null);
            if (tempUser != null) {
                auditService.logFailure(tempUser, "MFA_FAILED", "invalid_code",
                        "Invalid MFA code", ipAddress, userAgent);
            }
            throw new ApplicationException(ErrorCode.INVALID_MFA_CODE, "Invalid MFA code");
        }

        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));

        userService.handleSuccessfulLogin(user.getId());
        auditService.logSuccess(user, "MFA_SUCCESS", null, ipAddress, userAgent);

        return user;
    }

    @Transactional
    public void logout(String userId, String ipAddress, String userAgent) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND, userId));
        auditService.logSuccess(user, "LOGOUT", null, ipAddress, userAgent);
        log.info("User logged out successfully: {}", userId);
    }
}