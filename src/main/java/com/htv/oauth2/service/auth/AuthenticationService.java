package com.htv.oauth2.service.auth;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.LoginRequest;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.repository.UserRepository;
import com.htv.oauth2.service.AuditService;
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
    com.htv.oauth2.service.RateLimiterService rateLimiterService;

    /**
     * Authenticate user with username and password
     * Returns authenticated user or throws exception
     */
    @Transactional
    public User authenticateUser(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Authenticating user: {} from IP: {}", request.getUsername(), ipAddress);

        try {
            // Check rate limiting
            rateLimiterService.checkLoginRateLimit(ipAddress);

            // Find user
            User user = userRepository.findByUsernameOrEmail(request.getUsername())
                    .orElseThrow(() -> {
                        auditService.logAnonymous("LOGIN_FAILED", request.getUsername(),
                                "FAILURE", ipAddress, userAgent);
                        return new InvalidCredentialsException("Invalid username or password");
                    });

            // Check if account is locked
            if (user.isAccountLocked()) {
                log.warn("Login attempt on locked account: {}", request.getUsername());
                auditService.logFailure(user, "LOGIN_FAILED", "account_locked",
                        "Account is locked", ipAddress, userAgent);
                throw new AccountLockedException(
                        "Account is temporarily locked due to multiple failed login attempts. " +
                                "Please try again later or contact support."
                );
            }

            // Check if account is enabled
            if (!user.getEnabled()) {
                log.warn("Login attempt on disabled account: {}", request.getUsername());
                auditService.logFailure(user, "LOGIN_FAILED", "account_disabled",
                        "Account is disabled", ipAddress, userAgent);
                throw new AccountDisabledException("Account is disabled. Please contact support.");
            }

            // Check if email is verified (optional)
            if (!user.getEmailVerified()) {
                log.warn("Login attempt on unverified account: {}", request.getUsername());
                throw new EmailNotVerifiedException(
                        "Please verify your email address before logging in. " +
                                "Check your inbox for the verification link."
                );
            }

            // Verify password
            if (!passwordService.verifyPassword(request.getPassword(), user.getPassword())) {
                log.warn("Invalid password for user: {}", request.getUsername());
                userService.handleFailedLogin(request.getUsername());
                auditService.logFailure(user, "LOGIN_FAILED", "invalid_password",
                        "Invalid password", ipAddress, userAgent);
                throw new InvalidCredentialsException("Invalid username or password");
            }

            // Check if password needs rehashing (algorithm upgrade)
            if (passwordService.needsRehash(user.getPassword())) {
                log.info("Rehashing password for user: {}", user.getId());
                user.setPassword(passwordService.hashPassword(request.getPassword()));
                userRepository.persist(user);
            }

            // Handle MFA if enabled
            if (user.getMfaEnabled()) {
                if (request.getMfaCode() == null) {
                    // Generate temporary MFA session token
                    String mfaToken = mfaService.generateMfaSessionToken(user.getId());
                    log.info("MFA required for user: {}", user.getId());
                    throw new MfaRequiredException("MFA code required", mfaToken);
                }

                // Verify MFA code
                if (!mfaService.verifyMfaCode(user, request.getMfaCode())) {
                    log.warn("Invalid MFA code for user: {}", user.getId());
                    auditService.logFailure(user, "MFA_FAILED", "invalid_code",
                            "Invalid MFA code", ipAddress, userAgent);
                    throw new InvalidMfaCodeException("Invalid MFA code. Please try again.");
                }

                log.info("MFA verification successful for user: {}", user.getId());
            }

            // Update last login and reset failed attempts
            userService.handleSuccessfulLogin(user.getId());

            // Log successful login
            auditService.logSuccess(user, "LOGIN_SUCCESS", null, ipAddress, userAgent);

            log.info("User authenticated successfully: {}", user.getId());
            return user;

        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            auditService.logAnonymous("LOGIN_RATE_LIMIT", ipAddress,
                    "FAILURE", ipAddress, userAgent);
            throw e;
        }
    }

    /**
     * Validate user credentials for Resource Owner Password Credentials flow
     * Simpler validation without MFA
     */
    @Transactional
    public User validateCredentials(String username, String password) {
        log.debug("Validating credentials for: {}", username);

        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!user.getEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }

        if (user.isAccountLocked()) {
            throw new AccountLockedException("Account is locked");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }

    /**
     * Verify MFA session token and code
     */
    @Transactional
    public User verifyMfaSession(String mfaToken, String mfaCode, String ipAddress, String userAgent) {
        log.info("Verifying MFA session");

        // Validate MFA token and get user ID
        String userId = mfaService.validateMfaSessionToken(mfaToken);

        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new InvalidTokenException("Invalid MFA session"));

        // Verify MFA code
        if (!mfaService.verifyMfaCode(user, mfaCode)) {
            auditService.logFailure(user, "MFA_FAILED", "invalid_code",
                    "Invalid MFA code", ipAddress, userAgent);
            throw new InvalidMfaCodeException("Invalid MFA code");
        }

        // Update last login
        userService.handleSuccessfulLogin(user.getId());

        // Log successful MFA
        auditService.logSuccess(user, "MFA_SUCCESS", null, ipAddress, userAgent);

        return user;
    }

    /**
     * Logout user (revoke tokens)
     */
    @Transactional
    public void logout(String userId, String ipAddress, String userAgent) {
        log.info("Logging out user: {}", userId);

        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Revoke all user tokens
        // This will be handled by TokenService

        // Log logout
        auditService.logSuccess(user, "LOGOUT", null, ipAddress, userAgent);

        log.info("User logged out successfully: {}", userId);
    }
}