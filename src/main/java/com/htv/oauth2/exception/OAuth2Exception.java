package com.htv.oauth2.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2Exception extends RuntimeException {

    private final String error;
    private final String errorDescription;
    private final int httpStatus;
    private final Object additionalInfo;

    // Constructor cơ bản
    public OAuth2Exception(String error, String errorDescription, int httpStatus) {
        this(error, errorDescription, httpStatus, null, null);
    }

    // Constructor với Cause
    public OAuth2Exception(String error, String errorDescription, int httpStatus, Throwable cause) {
        this(error, errorDescription, httpStatus, null, cause);
    }

    // Constructor cho Additional Info
    public OAuth2Exception(String error, String errorDescription, int httpStatus, Object additionalInfo) {
        this(error, errorDescription, httpStatus, additionalInfo, null);
    }

    // Constructor đầy đủ
    public OAuth2Exception(String error, String errorDescription, int httpStatus, Object additionalInfo, Throwable cause) {
        super(errorDescription, cause);
        this.error = error;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
        this.additionalInfo = additionalInfo;
    }
}
