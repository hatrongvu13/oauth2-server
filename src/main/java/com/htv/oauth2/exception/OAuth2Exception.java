package com.htv.oauth2.exception;

import lombok.Getter;

@Getter
public class OAuth2Exception extends RuntimeException {

    private final String error;
    private final String errorDescription;
    private final int httpStatus;

    public OAuth2Exception(String error, String errorDescription, int httpStatus) {
        super(errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
    }

    public OAuth2Exception(String error, String errorDescription, int httpStatus, Throwable cause) {
        super(errorDescription, cause);
        this.error = error;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
    }
}
