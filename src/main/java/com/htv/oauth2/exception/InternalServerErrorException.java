package com.htv.oauth2.exception;

public class InternalServerErrorException extends OAuth2Exception {

    public InternalServerErrorException(String message) {
        super("server_error", message, 500);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super("server_error", message, 500, cause);
    }
}
