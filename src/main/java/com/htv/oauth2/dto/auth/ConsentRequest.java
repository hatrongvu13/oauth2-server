package com.htv.oauth2.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotEmpty(message = "At least one scope must be approved")
    private Set<String> approvedScopes;

    private Boolean rememberConsent;
}
