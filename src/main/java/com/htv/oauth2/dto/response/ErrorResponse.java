package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("error_uri")
    private String errorUri;

    private String message;

    private Integer status;

    private String path;

    private Instant timestamp;

    private Map<String, List<String>> validationErrors;

    @JsonProperty("trace_id")
    private String traceId;

    // Standard OAuth2 errors
    public static ErrorResponse invalidRequest(String description) {
        return ErrorResponse.builder()
                .error("invalid_request")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse invalidClient(String description) {
        return ErrorResponse.builder()
                .error("invalid_client")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse invalidGrant(String description) {
        return ErrorResponse.builder()
                .error("invalid_grant")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse unauthorizedClient(String description) {
        return ErrorResponse.builder()
                .error("unauthorized_client")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse unsupportedGrantType(String description) {
        return ErrorResponse.builder()
                .error("unsupported_grant_type")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse invalidScope(String description) {
        return ErrorResponse.builder()
                .error("invalid_scope")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse accessDenied(String description) {
        return ErrorResponse.builder()
                .error("access_denied")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse serverError(String description) {
        return ErrorResponse.builder()
                .error("server_error")
                .errorDescription(description)
                .timestamp(Instant.now())
                .build();
    }
}
