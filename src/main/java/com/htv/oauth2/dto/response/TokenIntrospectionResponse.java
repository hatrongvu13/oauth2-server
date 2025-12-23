package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class TokenIntrospectionResponse {

    private Boolean active;

    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    private String username;

    @JsonProperty("token_type")
    private String tokenType;

    private Long exp; // Expiration timestamp

    private Long iat; // Issued at timestamp

    private Long nbf; // Not before timestamp

    private String sub; // Subject (user ID)

    private String aud; // Audience

    private String iss; // Issuer

    private String jti; // JWT ID
}
