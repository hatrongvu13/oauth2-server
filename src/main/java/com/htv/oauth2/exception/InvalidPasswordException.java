package com.htv.oauth2.exception;

public class InvalidPasswordException extends OAuth2Exception {

    public InvalidPasswordException(String message) {
        super("invalid_password", message, 400);
    }
}
