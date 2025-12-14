package com.htv.oauth2.exception;

public class InvalidGrantException extends OAuth2Exception {

    public InvalidGrantException(String message) {
        super("invalid_grant", message, 400);
    }
}
