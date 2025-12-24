package com.htv.oauth2.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception duy nhất cho toàn bộ ứng dụng
 * Sử dụng ErrorCode enum để quản lý lỗi
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] messageParams;
    private final Map<String, Object> metadata;

    // Constructor cơ bản
    public ApplicationException(ErrorCode errorCode) {
        this(errorCode, null, null, null);
    }

    // Constructor với params cho message
    public ApplicationException(ErrorCode errorCode, Object... messageParams) {
        this(errorCode, messageParams, null, null);
    }

    // Constructor với cause
    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, null, cause);
    }

    // Constructor với metadata
    public ApplicationException(ErrorCode errorCode, Object[] messageParams, Map<String, Object> metadata) {
        this(errorCode, messageParams, metadata, null);
    }

    // Constructor đầy đủ
    public ApplicationException(ErrorCode errorCode, Object[] messageParams, Map<String, Object> metadata, Throwable cause) {
        super(errorCode.getMessageKey(), cause);
        this.errorCode = errorCode;
        this.messageParams = messageParams;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Helper methods để thêm metadata
    public ApplicationException withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public ApplicationException withMetadata(Map<String, Object> additionalMetadata) {
        if (additionalMetadata != null) {
            this.metadata.putAll(additionalMetadata);
        }
        return this;
    }

    // Helper đặc biệt cho MFA token
    public ApplicationException withMfaToken(String mfaToken) {
        this.metadata.put("mfa_token", mfaToken);
        return this;
    }

    // Helper cho session/state info
    public ApplicationException withSessionInfo(String sessionId, String state) {
        this.metadata.put("session_id", sessionId);
        this.metadata.put("state", state);
        return this;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public String getMessageKey() {
        return errorCode.getMessageKey();
    }
}