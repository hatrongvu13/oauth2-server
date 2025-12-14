package com.htv.oauth2.exception;

public class UnauthorizedClientException extends OAuth2Exception {

    public UnauthorizedClientException(String message) {
        super("unauthorized_client", message, 403);
    }
}
