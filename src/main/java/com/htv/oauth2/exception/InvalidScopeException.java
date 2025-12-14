package com.htv.oauth2.exception;

public class InvalidScopeException extends OAuth2Exception {

    public InvalidScopeException(String message) {
        super("invalid_scope", message, 400);
    }
}
