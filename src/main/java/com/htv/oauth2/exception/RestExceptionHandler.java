package com.htv.oauth2.exception;

import com.htv.oauth2.dto.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Custom exception handler for REST responses
 * This can be used in specific resources if needed
 */
@Slf4j
public class RestExceptionHandler {

    /**
     * Convert OAuth2Exception to JAX-RS Response
     */
    public static Response toResponse(OAuth2Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ex.getError())
                .errorDescription(ex.getErrorDescription())
                .message(ex.getMessage())
                .status(ex.getHttpStatus())
                .timestamp(Instant.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        Response.ResponseBuilder builder = Response
                .status(ex.getHttpStatus())
                .entity(errorResponse);

        // Add WWW-Authenticate header for 401 responses
        if (ex.getHttpStatus() == 401) {
            builder.header("WWW-Authenticate", "Bearer");
        }

        // Add Retry-After header for rate limiting
        if (ex instanceof RateLimitExceededException rateLimitEx) {
            builder.header("Retry-After", rateLimitEx.getRetryAfter())
                    .header("X-RateLimit-Remaining", 0)
                    .header("X-RateLimit-Reset", Instant.now().getEpochSecond() + rateLimitEx.getRetryAfter());
            ;
        }

        return builder.build();
    }

    /**
     * Convert validation errors to Response
     */
    public static Response validationErrorResponse(Map<String, List<String>> errors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("validation_error")
                .errorDescription("Request validation failed")
                .message("Invalid request parameters")
                .status(400)
                .validationErrors(errors)
                .timestamp(Instant.now())
                .traceId(UUID.randomUUID().toString())
                .build();

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }

    /**
     * Success response helper
     */
    public static Response successResponse(Object data) {
        return Response.ok(data).build();
    }

    /**
     * Created response helper
     */
    public static Response createdResponse(Object data, String location) {
        return Response
                .status(Response.Status.CREATED)
                .entity(data)
                .header("Location", location)
                .build();
    }

    /**
     * No content response helper
     */
    public static Response noContentResponse() {
        return Response.noContent().build();
    }
}
