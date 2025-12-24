package com.htv.oauth2.dto.request.mfa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerifyRequest {

    @NotBlank(message = "MFA session token is required")
    private String mfaToken;

    @NotBlank(message = "MFA code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "MFA code must be 6 digits")
    private String mfaCode;
}
