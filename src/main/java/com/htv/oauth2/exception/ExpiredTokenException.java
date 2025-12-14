package com.htv.oauth2.exception;

public class ExpiredTokenException extends OAuth2Exception {

    public ExpiredTokenException(String message) {
        super("expired_token", message, 401);
    }
}
