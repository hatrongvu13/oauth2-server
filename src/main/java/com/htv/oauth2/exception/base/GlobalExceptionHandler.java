package com.htv.oauth2.exception.base;

import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;
import com.htv.oauth2.exception.security.RateLimitExceededException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;
import java.util.*;

/**
 * Global exception handler for OAuth2 server
 * Converts exceptions to appropriate HTTP responses
 */
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle OAuth2 specific exceptions
     */
    @ServerExceptionMapper
    public RestResponse<Object> handleOAuth2Exception(OAuth2Exception ex) {
        log.error("OAuth2 error: {} - {}", ex.getError(), ex.getErrorDescription(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ex.getError())
                .errorDescription(ex.getErrorDescription())
                .message(ex.getMessage())
                .status(ex.getHttpStatus())
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        // Add Retry-After header for rate limiting
        if (ex instanceof RateLimitExceededException rateLimitEx) {
            return RestResponse.ResponseBuilder
                    .create(ex.getHttpStatus(), String.valueOf(errorResponse))
                    .header("Retry-After", String.valueOf(rateLimitEx.getRetryAfter()))
                    .header("X-RateLimit-Remaining", 0)
                    .header("X-RateLimit-Reset", Instant.now().getEpochSecond() + rateLimitEx.getRetryAfter())
                    .build();
        }

        return RestResponse.status(Response.Status.fromStatusCode(ex.getHttpStatus()), errorResponse);
    }

    /**
     * Handle validation exceptions (Bean Validation)
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleValidationException(ConstraintViolationException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, List<String>> validationErrors = new HashMap<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = getFieldName(violation.getPropertyPath().toString());
            validationErrors.computeIfAbsent(fieldName, k -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("validation_error")
                .errorDescription("Request validation failed")
                .message("Invalid request parameters")
                .status(400)
                .validationErrors(validationErrors)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("invalid_request")
                .errorDescription(ex.getMessage())
                .message("Invalid request")
                .status(400)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse);
    }

    /**
     * Handle IllegalStateException
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("invalid_state")
                .errorDescription(ex.getMessage())
                .message("Invalid operation state")
                .status(409)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.CONFLICT, errorResponse);
    }

    /**
     * Handle NullPointerException
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("server_error")
                .errorDescription("An unexpected error occurred")
                .message("Internal server error")
                .status(500)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, errorResponse);
    }

    /**
     * Handle generic exceptions (fallback)
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("server_error")
                .errorDescription("An unexpected error occurred")
                .message("Internal server error")
                .status(500)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, errorResponse);
    }

    /**
     * Extract field name from property path
     */
    private String getFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return "unknown";
        }

        // Extract the last segment of the path
        String[] segments = propertyPath.split("\\.");
        return segments[segments.length - 1];
    }

    /**
     * Generate a unique trace ID for error tracking
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}

