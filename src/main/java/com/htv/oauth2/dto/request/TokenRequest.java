package com.htv.oauth2.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    @NotBlank(message = "Grant type is required")
    @JsonProperty("grant_type")
    private String grantType; // authorization_code, refresh_token, client_credentials, password

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    private String code; // For authorization_code grant

    @JsonProperty("redirect_uri")
    private String redirectUri; // For authorization_code grant

    @JsonProperty("refresh_token")
    private String refreshToken; // For refresh_token grant

    private String username; // For password grant

    private String password; // For password grant

    private String scope;

    @JsonProperty("code_verifier")
    private String codeVerifier; // PKCE verifier
}
