package com.htv.oauth2.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegistrationRequest {

    @NotBlank(message = "Client name is required")
    @Size(min = 3, max = 255, message = "Client name must be between 3 and 255 characters")
    private String clientName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotEmpty(message = "At least one redirect URI is required")
    private Set<@NotBlank @Pattern(regexp = "^https?://.*", message = "Invalid redirect URI") String> redirectUris;

    @NotEmpty(message = "At least one grant type is required")
    private Set<@NotBlank String> grantTypes;

    private Set<String> scopes;

    @Min(value = 300, message = "Access token validity must be at least 300 seconds (5 minutes)")
    @Max(value = 86400, message = "Access token validity must not exceed 86400 seconds (24 hours)")
    private Integer accessTokenValidity; // in seconds

    @Min(value = 3600, message = "Refresh token validity must be at least 3600 seconds (1 hour)")
    @Max(value = 2592000, message = "Refresh token validity must not exceed 2592000 seconds (30 days)")
    private Integer refreshTokenValidity; // in seconds

    private Boolean autoApprove;
}
