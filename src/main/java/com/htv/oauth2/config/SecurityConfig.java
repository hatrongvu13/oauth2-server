package com.htv.oauth2.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Getter
@ApplicationScoped
public class SecurityConfig {

    @ConfigProperty(name = "oauth2.security.max-failed-login-attempts", defaultValue = "5")
    Integer maxFailedLoginAttempts;

    @ConfigProperty(name = "oauth2.security.account-lock-duration", defaultValue = "900")
    Long accountLockDuration; // seconds

    @ConfigProperty(name = "oauth2.security.password-min-length", defaultValue = "8")
    Integer passwordMinLength;

    @ConfigProperty(name = "oauth2.features.mfa-enabled", defaultValue = "true")
    Boolean mfaEnabled;

    @ConfigProperty(name = "oauth2.features.pkce-required", defaultValue = "true")
    Boolean pkceRequired;
}
