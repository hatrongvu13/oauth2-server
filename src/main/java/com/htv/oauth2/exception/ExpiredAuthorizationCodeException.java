package com.htv.oauth2.exception;

public class ExpiredAuthorizationCodeException extends OAuth2Exception {

    public ExpiredAuthorizationCodeException(String message) {
        super("expired_authorization_code", message, 400);
    }
}
