package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class ConsentResponse {

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("client_description")
    private String clientDescription;

    @JsonProperty("requested_scopes")
    private Set<ScopeInfo> requestedScopes;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    private String state;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScopeInfo {
        private String name;
        private String description;
        private Boolean required;
    }
}
