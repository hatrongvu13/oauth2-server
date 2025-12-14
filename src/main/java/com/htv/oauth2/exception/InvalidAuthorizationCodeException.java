package com.htv.oauth2.exception;

public class InvalidAuthorizationCodeException extends OAuth2Exception {

    public InvalidAuthorizationCodeException(String message) {
        super("invalid_authorization_code", message, 400);
    }
}
