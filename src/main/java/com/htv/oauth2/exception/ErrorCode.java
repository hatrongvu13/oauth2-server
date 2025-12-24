package com.htv.oauth2.exception;

import lombok.Getter;

/**
 * Tập trung tất cả error codes của hệ thống
 * Hỗ trợ đa ngôn ngữ và mở rộng dễ dàng
 */
@Getter
public enum ErrorCode {

    // ===== OAuth2 Errors (4000-4099) =====
    INVALID_REQUEST("ERR_4000", "error.invalid_request", 400),
    INVALID_CLIENT("ERR_4001", "error.invalid_client", 401),
    INVALID_GRANT("ERR_4002", "error.invalid_grant", 400),
    UNAUTHORIZED_CLIENT("ERR_4003", "error.unauthorized_client", 401),
    UNSUPPORTED_GRANT_TYPE("ERR_4004", "error.unsupported_grant_type", 400),
    UNSUPPORTED_RESPONSE_TYPE("ERR_4005", "error.unsupported_response_type", 400),
    INVALID_SCOPE("ERR_4006", "error.invalid_scope", 400),
    CONSENT_REQUIRED("ERR_4007", "error.consent_required", 403),

    // ===== Token Errors (4100-4199) =====
    INVALID_TOKEN("ERR_4100", "error.invalid_token", 401),
    EXPIRED_TOKEN("ERR_4101", "error.expired_token", 401),
    TOKEN_REVOKED("ERR_4102", "error.token_revoked", 401),
    INVALID_AUTHORIZATION_CODE("ERR_4103", "error.invalid_authorization_code", 400),
    EXPIRED_AUTHORIZATION_CODE("ERR_4104", "error.expired_authorization_code", 400),

    // ===== Authentication Errors (4200-4299) =====
    INVALID_CREDENTIALS("ERR_4200", "error.invalid_credentials", 401),
    INVALID_PASSWORD("ERR_4201", "error.invalid_password", 401),
    PASSWORD_MISMATCH("ERR_4202", "error.password_mismatch", 400),

    // ===== MFA Errors (4300-4399) =====
    MFA_REQUIRED("ERR_4300", "error.mfa_required", 403, true), // Cần trả thêm mfa_token
    INVALID_MFA_CODE("ERR_4301", "error.invalid_mfa_code", 401),
    EXPIRED_MFA_TOKEN("ERR_4302", "error.expired_mfa_token", 401),

    // ===== User Errors (4400-4499) =====
    USER_NOT_FOUND("ERR_4400", "error.user_not_found", 404),
    USER_ALREADY_EXISTS("ERR_4401", "error.user_already_exists", 409),
    EMAIL_ALREADY_EXISTS("ERR_4402", "error.email_already_exists", 409),
    USERNAME_ALREADY_EXISTS("ERR_4403", "error.username_already_exists", 409),
    ACCOUNT_LOCKED("ERR_4404", "error.account_locked", 403),
    ACCOUNT_DISABLED("ERR_4405", "error.account_disabled", 403),
    EMAIL_NOT_VERIFIED("ERR_4406", "error.email_not_verified", 403),

    // ===== Client Errors (4500-4599) =====
    CLIENT_NOT_FOUND("ERR_4500", "error.client_not_found", 404),
    CLIENT_NOT_REGISTERED_URI("ERR_4501", "error.error.redirect_url", 404),

    // ===== Security Errors (4600-4699) =====
    UNAUTHORIZED("ERR_4600", "error.unauthorized", 401),
    ACCESS_DENIED("ERR_4601", "error.access_denied", 403),
    RATE_LIMIT_EXCEEDED("ERR_4602", "error.rate_limit_exceeded", 429, true),
    INVALID_OPERATION("ERR_4603", "error.invalid_operation", 400),

    // ===== Resource Errors (4700-4799) =====
    RESOURCE_NOT_FOUND("ERR_4700", "error.resource_not_found", 404),
    DUPLICATE_RESOURCE("ERR_4701", "error.duplicate_resource", 409),

    // ===== PKCE Errors (4800-4899) =====
    INVALID_CODE_CHALLENGE("ERR_4800", "error.invalid_code_challenge", 400),
    INVALID_CODE_VERIFIER("ERR_4801", "error.invalid_code_verifier", 400),
    CODE_CHALLENGE_MISMATCH("ERR_4802", "error.code_challenge_mismatch", 400),

    // ===== Validation Errors (4900-4999) =====
    VALIDATION_ERROR("ERR_4900", "error.validation_error", 400),

    // ===== System Errors (5000-5099) =====
    INTERNAL_SERVER_ERROR("ERR_5000", "error.internal_server_error", 500),
    SERVICE_UNAVAILABLE("ERR_5001", "error.service_unavailable", 503),
    DATABASE_ERROR("ERR_5002", "error.database_error", 500),
    CACHE_ERROR("ERR_5003", "error.cache_error", 500);

    private final String code;
    private final String messageKey;
    private final int httpStatus;
    private final boolean requiresAdditionalInfo; // Flag cho các error cần trả thêm data

    ErrorCode(String code, String messageKey, int httpStatus) {
        this(code, messageKey, httpStatus, false);
    }

    ErrorCode(String code, String messageKey, int httpStatus, boolean requiresAdditionalInfo) {
        this.code = code;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
        this.requiresAdditionalInfo = requiresAdditionalInfo;
    }

    /**
     * Lấy error code theo string code
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}