package com.htv.oauth2.exception.core;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InternalServerErrorException extends OAuth2Exception {

    public InternalServerErrorException(String message) {
        super("server_error", message, 500);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super("server_error", message, 500, cause);
    }
}
