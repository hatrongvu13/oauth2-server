package com.htv.oauth2.exception;

public class InvalidOperationException extends OAuth2Exception {

    public InvalidOperationException(String message) {
        super("invalid_operation", message, 400);
    }
}
