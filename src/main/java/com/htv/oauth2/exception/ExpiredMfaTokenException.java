package com.htv.oauth2.exception;

public class ExpiredMfaTokenException extends OAuth2Exception {
    public ExpiredMfaTokenException(String message) {
        super("mfa_token", message, 410);
    }
}
