package com.htv.oauth2.exception;

public class InvalidCredentialsException extends OAuth2Exception {

    public InvalidCredentialsException(String message) {
        super("invalid_credentials", message, 401);
    }
}
