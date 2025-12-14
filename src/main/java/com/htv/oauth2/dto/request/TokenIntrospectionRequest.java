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
public class TokenIntrospectionRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @JsonProperty("token_type_hint")
    private String tokenTypeHint; // access_token or refresh_token
}
