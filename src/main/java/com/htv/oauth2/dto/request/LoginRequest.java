package com.htv.oauth2.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{6}$", message = "MFA code must be 6 digits")
    private String mfaCode;

    private Boolean rememberMe;
}

