package com.htv.oauth2.exception;

public class InvalidClientException extends OAuth2Exception {

    public InvalidClientException(String message) {
        super("invalid_client", message, 401);
    }
}
