package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationResponse {

    private String code; // Authorization code

    private String state; // CSRF protection token

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("expires_in")
    private Long expiresIn;
}
