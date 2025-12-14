package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType; // "Bearer"

    @JsonProperty("expires_in")
    private Long expiresIn; // seconds

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    private UserResponse user;

    @JsonProperty("mfa_required")
    private Boolean mfaRequired;

    @JsonProperty("mfa_token")
    private String mfaToken; // Temporary token for MFA flow
}
