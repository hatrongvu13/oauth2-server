package com.htv.oauth2.exception.auth.oauth2;

public class InvalidClientException extends OAuth2Exception {

    public InvalidClientException(String message) {
        super("invalid_client", message, 401);
    }
}
