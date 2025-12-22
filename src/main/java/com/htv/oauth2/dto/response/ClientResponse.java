package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class ClientResponse {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret; // Only returned on creation/reset

    @JsonProperty("client_name")
    private String clientName;

    private String description;

    @JsonProperty("redirect_uris")
    private Set<String> redirectUris;

    @JsonProperty("grant_types")
    private Set<String> grantTypes;

    private Set<String> scopes;

    @JsonProperty("access_token_validity")
    private Integer accessTokenValidity;

    @JsonProperty("refresh_token_validity")
    private Integer refreshTokenValidity;

    @JsonProperty("auto_approve")
    private Boolean autoApprove;

    private Boolean enabled;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;
}
