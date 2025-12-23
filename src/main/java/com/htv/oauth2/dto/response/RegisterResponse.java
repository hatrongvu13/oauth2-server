package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class RegisterResponse {

    private String userId;

    private String username;

    private String email;

    @JsonProperty("email_verification_required")
    private Boolean emailVerificationRequired;

    private String message;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("mfa_required_setup")
    private boolean mfaRequiredSetup;

    @JsonProperty("mfa_secret_key")
    private String mfaSecretKey;

    @JsonProperty("mfa_qr_code_url")
    private String mfaQrCodeUrl;

}
