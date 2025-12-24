package com.htv.oauth2.dto.auth;

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
public class AuthorizationRequest {

    @NotBlank(message = "Response type is required")
    @JsonProperty("response_type")
    private String responseType; // "code" for authorization code flow

    @NotBlank(message = "Client ID is required")
    @JsonProperty("client_id")
    private String clientId;

    @NotBlank(message = "Redirect URI is required")
    @JsonProperty("redirect_uri")
    private String redirectUri;

    private String scope;

    private String state; // CSRF protection

    @JsonProperty("code_challenge")
    private String codeChallenge; // PKCE

    @JsonProperty("code_challenge_method")
    private String codeChallengeMethod; // S256 or plain
}
