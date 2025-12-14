package com.htv.oauth2.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateRequest {

    @Size(min = 3, max = 255, message = "Client name must be between 3 and 255 characters")
    private String clientName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Set<@Pattern(regexp = "^https?://.*", message = "Invalid redirect URI") String> redirectUris;

    private Set<String> grantTypes;

    private Set<String> scopes;

    @Min(value = 300, message = "Access token validity must be at least 300 seconds")
    @Max(value = 86400, message = "Access token validity must not exceed 86400 seconds")
    private Integer accessTokenValidity;

    @Min(value = 3600, message = "Refresh token validity must be at least 3600 seconds")
    @Max(value = 2592000, message = "Refresh token validity must not exceed 2592000 seconds")
    private Integer refreshTokenValidity;

    private Boolean autoApprove;

    private Boolean enabled;
}
