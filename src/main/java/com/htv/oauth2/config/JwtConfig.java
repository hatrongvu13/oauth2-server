package com.htv.oauth2.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Getter
@ApplicationScoped
public class JwtConfig {

    @ConfigProperty(name = "oauth2.jwt.issuer", defaultValue = "https://oauth2.htv.com")
    String issuer;

    @ConfigProperty(name = "oauth2.jwt.access-token-expiry", defaultValue = "3600")
    Long accessTokenExpiry; // seconds

    @ConfigProperty(name = "oauth2.jwt.refresh-token-expiry", defaultValue = "86400")
    Long refreshTokenExpiry; // seconds

    @ConfigProperty(name = "smallrye.jwt.sign.key.location", defaultValue = "/privateKey.pem")
    String privateKeyLocation;

    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "/publicKey.pem")
    String publicKeyLocation;
}
