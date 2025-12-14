package com.htv.oauth2.exception;

public class InvalidRequestException extends OAuth2Exception {

    public InvalidRequestException(String message) {
        super("invalid_request", message, 400);
    }
}
