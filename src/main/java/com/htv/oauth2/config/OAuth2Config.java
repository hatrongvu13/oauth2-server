package com.htv.oauth2.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Getter
@ApplicationScoped
public class OAuth2Config {

    // Authorization Code
    @ConfigProperty(name = "oauth2.authorization-code.validity", defaultValue = "300")
    Integer authorizationCodeValidity; // seconds

    // Rate Limiting - Login
    @ConfigProperty(name = "oauth2.rate-limit.login.max-attempts", defaultValue = "5")
    Integer loginMaxAttempts;

    @ConfigProperty(name = "oauth2.rate-limit.login.window-seconds", defaultValue = "900")
    Long loginWindowSeconds;

    // Rate Limiting - Token
    @ConfigProperty(name = "oauth2.rate-limit.token.max-requests", defaultValue = "100")
    Integer tokenMaxRequests;

    @ConfigProperty(name = "oauth2.rate-limit.token.window-seconds", defaultValue = "3600")
    Long tokenWindowSeconds;

    // Features
    @ConfigProperty(name = "oauth2.features.mfa-enabled", defaultValue = "true")
    Boolean mfaEnabled;

    @ConfigProperty(name = "oauth2.features.pkce-required", defaultValue = "false")
    Boolean pkceRequired;
}
