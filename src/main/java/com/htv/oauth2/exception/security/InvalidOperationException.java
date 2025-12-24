package com.htv.oauth2.exception.security;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InvalidOperationException extends OAuth2Exception {

    public InvalidOperationException(String message) {
        super("invalid_operation", message, 400);
    }
}
