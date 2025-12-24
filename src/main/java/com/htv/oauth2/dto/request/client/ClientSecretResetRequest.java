package com.htv.oauth2.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSecretResetRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;
}
