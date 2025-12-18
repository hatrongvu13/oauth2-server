//package com.htv.oauth2.config;
//
//import io.quarkus.runtime.StartupEvent;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.enterprise.event.Observes;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.microprofile.config.inject.ConfigProperty;
//
//@Slf4j
//@Getter
//@ApplicationScoped
//public class OAuth2Config {
//
//    // Authorization Code
//    @ConfigProperty(name = "oauth2.authorization-code.validity", defaultValue = "300")
//    Integer authorizationCodeValidity; // seconds
//
//    // Rate Limiting - Login
//    @ConfigProperty(name = "oauth2.rate-limit.login.max-attempts", defaultValue = "5")
//    Integer loginMaxAttempts;
//
//    @ConfigProperty(name = "oauth2.rate-limit.login.window-seconds", defaultValue = "900")
//    Long loginWindowSeconds;
//
//    // Rate Limiting - Token
//    @ConfigProperty(name = "oauth2.rate-limit.token.max-requests", defaultValue = "100")
//    Integer tokenMaxRequests;
//
//    @ConfigProperty(name = "oauth2.rate-limit.token.window-seconds", defaultValue = "3600")
//    Long tokenWindowSeconds;
//
//    // Account Security
//    @ConfigProperty(name = "oauth2.security.max-failed-login-attempts", defaultValue = "5")
//    Integer maxFailedLoginAttempts;
//
//    @ConfigProperty(name = "oauth2.security.account-lock-duration", defaultValue = "900")
//    Long accountLockDuration; // seconds
//
//    @ConfigProperty(name = "oauth2.security.password-min-length", defaultValue = "8")
//    Integer passwordMinLength;
//
//    // Features
//    @ConfigProperty(name = "oauth2.features.mfa-enabled", defaultValue = "true")
//    Boolean mfaEnabled;
//
//    @ConfigProperty(name = "oauth2.features.pkce-required", defaultValue = "false")
//    Boolean pkceRequired;
//
//    // Session
//    @ConfigProperty(name = "oauth2.session.ttl", defaultValue = "3600")
//    Long sessionTtl; // seconds
//
//    // MFA
//    @ConfigProperty(name = "oauth2.mfa.time-step", defaultValue = "30")
//    Integer mfaTimeStep; // seconds
//
//    @ConfigProperty(name = "oauth2.mfa.code-length", defaultValue = "6")
//    Integer mfaCodeLength;
//
//    @ConfigProperty(name = "oauth2.mfa.session-validity", defaultValue = "300")
//    Long mfaSessionValidity; // seconds
//
//    // Log configuration on startup
//    void onStart(@Observes StartupEvent ev) {
//        log.info("OAuth2 Configuration:");
//        log.info("  Authorization Code Validity: {}s", authorizationCodeValidity);
//        log.info("  Login Rate Limit: {} attempts / {}s", loginMaxAttempts, loginWindowSeconds);
//        log.info("  Token Rate Limit: {} requests / {}s", tokenMaxRequests, tokenWindowSeconds);
//        log.info("  Max Failed Login Attempts: {}", maxFailedLoginAttempts);
//        log.info("  Account Lock Duration: {}s", accountLockDuration);
//        log.info("  Password Min Length: {}", passwordMinLength);
//        log.info("  MFA Enabled: {}", mfaEnabled);
//        log.info("  PKCE Required: {}", pkceRequired);
//        log.info("  Session TTL: {}s", sessionTtl);
//        log.info("  MFA Time Step: {}s", mfaTimeStep);
//        log.info("  MFA Code Length: {}", mfaCodeLength);
//        log.info("  MFA Session Validity: {}s", mfaSessionValidity);
//    }
//}