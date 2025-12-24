package com.htv.oauth2.exception;

import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.service.i18n.MessageService;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Global exception handler với hỗ trợ đa ngôn ngữ
 */
@Slf4j
public class GlobalExceptionHandler {

    @Inject
    MessageService messageService;

    @Context
    HttpHeaders headers;

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleApplicationException(ApplicationException ex) {
        Locale locale = getLocaleFromHeaders();
        logException(ex);

        String localizedMessage = messageService.getMessage(
                ex.getMessageKey(),
                locale,
                ex.getMessageParams()
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ex.getCode())
                .errorDescription(localizedMessage)
                .message(localizedMessage)
                .status(ex.getHttpStatus())
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .additionalInfo(buildAdditionalInfo(ex)) // Thêm additional info
                .build();

        RestResponse.ResponseBuilder<ErrorResponse> builder =
                RestResponse.ResponseBuilder.create(
                        Response.Status.fromStatusCode(ex.getHttpStatus()),
                        errorResponse
                );

        // Thêm metadata headers
        addMetadataHeaders(builder, ex);

        // Xử lý rate limiting
        if (ex.getErrorCode() == ErrorCode.RATE_LIMIT_EXCEEDED) {
            addRateLimitHeaders(builder, ex);
        }

        // Thêm WWW-Authenticate cho 401
        if (ex.getHttpStatus() == 401) {
            builder.header("WWW-Authenticate", "Bearer");
        }

        return builder.build();
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleValidationException(ConstraintViolationException ex) {
        Locale locale = getLocaleFromHeaders();
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, List<String>> validationErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        violation -> extractFieldName(violation.getPropertyPath().toString()),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        String localizedMessage = messageService.getMessage("error.validation_error", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorCode.VALIDATION_ERROR.getCode())
                .errorDescription(localizedMessage)
                .message(localizedMessage)
                .status(400)
                .validationErrors(validationErrors)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        Locale locale = getLocaleFromHeaders();
        log.warn("Illegal argument: {}", ex.getMessage());

        String localizedMessage = messageService.getMessage("error.invalid_request", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorCode.INVALID_REQUEST.getCode())
                .errorDescription(localizedMessage)
                .message(ex.getMessage())
                .status(400)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleGenericException(Exception ex) {
        Locale locale = getLocaleFromHeaders();
        log.error("Unhandled exception: {}", ex.getClass().getName(), ex);

        String localizedMessage = messageService.getMessage("error.internal_server_error", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .errorDescription(localizedMessage)
                .message(localizedMessage)
                .status(500)
                .timestamp(Instant.now())
                .traceId(generateTraceId())
                .build();

        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, errorResponse);
    }

    // ===== Helper Methods =====

    private Map<String, Object> buildAdditionalInfo(ApplicationException ex) {
        Map<String, Object> metadata = ex.getMetadata();

        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        Map<String, Object> additionalInfo = new HashMap<>();

        // Xử lý đặc biệt cho MFA_REQUIRED
        if (ex.getErrorCode() == ErrorCode.MFA_REQUIRED) {
            if (metadata.containsKey("mfa_token")) {
                additionalInfo.put("mfa_token", metadata.get("mfa_token"));
            }
            if (metadata.containsKey("session_id")) {
                additionalInfo.put("session_id", metadata.get("session_id"));
            }
            if (metadata.containsKey("state")) {
                additionalInfo.put("state", metadata.get("state"));
            }
            // Thêm các thông tin hữu ích cho MFA
            if (metadata.containsKey("mfa_methods")) {
                additionalInfo.put("mfa_methods", metadata.get("mfa_methods"));
            }
            if (metadata.containsKey("expires_in")) {
                additionalInfo.put("expires_in", metadata.get("expires_in"));
            }
        }
        // Các error code khác cần additional info
        else if (ex.getErrorCode().isRequiresAdditionalInfo()) {
            additionalInfo.putAll(metadata);
        }
        // Metadata thông thường (không sensitive)
        else {
            // Chỉ thêm metadata không sensitive vào response
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (!isSensitiveMetadata(entry.getKey())) {
                    additionalInfo.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return additionalInfo.isEmpty() ? null : additionalInfo;
    }

    private boolean isSensitiveMetadata(String key) {
        // Các metadata sensitive không được trả về trong response
        return key.equals("password") ||
                key.equals("secret") ||
                key.equals("private_key") ||
                key.startsWith("internal_");
    }

    private void addMetadataHeaders(RestResponse.ResponseBuilder<?> builder, ApplicationException ex) {
        Map<String, Object> metadata = ex.getMetadata();
        if (metadata != null && !metadata.isEmpty()) {
            metadata.forEach((key, value) -> {
                if (value != null) {
                    builder.header("X-Metadata-" + key, String.valueOf(value));
                }
            });
        }
    }

    private void addRateLimitHeaders(RestResponse.ResponseBuilder<?> builder, ApplicationException ex) {
        Map<String, Object> metadata = ex.getMetadata();
        Long retryAfter = (Long) metadata.getOrDefault("retryAfter", 60L);
        long resetTime = Instant.now().getEpochSecond() + retryAfter;

        builder.header("Retry-After", String.valueOf(retryAfter))
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(resetTime));
    }

    private void logException(ApplicationException ex) {
        if (ex.getHttpStatus() >= 500) {
            log.error("Server error [{}]: {}", ex.getCode(), ex.getMessage(), ex);
        } else if (ex.getHttpStatus() >= 400) {
            log.warn("Client error [{}]: {}", ex.getCode(), ex.getMessage());
        }
    }

    private Locale getLocaleFromHeaders() {
        if (headers == null) {
            return Locale.ENGLISH;
        }

        List<Locale> acceptableLanguages = headers.getAcceptableLanguages();
        if (acceptableLanguages != null && !acceptableLanguages.isEmpty()) {
            return acceptableLanguages.get(0);
        }

        String langHeader = headers.getHeaderString("Accept-Language");
        if (langHeader != null) {
            return messageService.parseLocale(langHeader.split(",")[0].trim());
        }

        return Locale.ENGLISH;
    }

    private String extractFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return "unknown";
        }
        String[] segments = propertyPath.split("\\.");
        return segments[segments.length - 1];
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}