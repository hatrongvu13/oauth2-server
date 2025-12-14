package com.htv.oauth2.service.auth;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.LoginRequest;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.repository.UserRepository;
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

    /**
     * Authenticate user with username and password
     */
    @Transactional
    public User authenticateUser(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        // Find user
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts");
        }

        // Check if account is enabled
        if (!user.getEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }

        // Verify password
        if (!passwordService.verifyPassword(request.getPassword(), user.getPassword())) {
            userService.handleFailedLogin(request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Check MFA if enabled
        if (user.getMfaEnabled()) {
            if (request.getMfaCode() == null) {
                throw new MfaRequiredException("MFA code required", generateMfaToken(user));
            }

            if (!verifyMfaCode(user, request.getMfaCode())) {
                throw new InvalidMfaCodeException("Invalid MFA code");
            }
        }

        // Update last login
        userService.handleSuccessfulLogin(user.getId());

        log.info("User authenticated successfully: {}", user.getId());
        return user;
    }

    /**
     * Validate user credentials for Resource Owner Password Credentials flow
     */
    public User validateCredentials(String username, String password) {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!user.getEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }

    private String generateMfaToken(User user) {
        // Generate temporary MFA token (valid for 5 minutes)
        return com.htv.oauth2.util.CryptoUtil.generateSecureToken(32);
    }

    private boolean verifyMfaCode(User user, String code) {
        // Implement TOTP verification
        // This is a placeholder - implement with a TOTP library
        return true;
    }
}