package com.htv.oauth2.exception;

public class InvalidMfaCodeException extends OAuth2Exception {

    public InvalidMfaCodeException(String message) {
        super("invalid_mfa_code", message, 401);
    }
}
