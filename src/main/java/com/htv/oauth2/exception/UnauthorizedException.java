package com.htv.oauth2.exception;

public class UnauthorizedException extends OAuth2Exception {
    public UnauthorizedException(String message) {
        super("unauthorized", message, 401);
    }
}
