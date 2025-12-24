package com.htv.oauth2.exception.auth.oauth2;

public class UnauthorizedClientException extends OAuth2Exception {

    public UnauthorizedClientException(String message) {
        super("unauthorized_client", message, 403);
    }
}
