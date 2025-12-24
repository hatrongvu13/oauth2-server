package com.htv.oauth2.exception.auth.token;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InvalidTokenException extends OAuth2Exception {

    public InvalidTokenException(String message) {
        super("invalid_token", message, 401);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super("invalid_token", message, 401, cause);
    }
}
