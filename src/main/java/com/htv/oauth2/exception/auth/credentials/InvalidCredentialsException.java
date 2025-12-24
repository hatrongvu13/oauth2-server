package com.htv.oauth2.exception.auth.credentials;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InvalidCredentialsException extends OAuth2Exception {

    public InvalidCredentialsException(String message) {
        super("invalid_credentials", message, 401);
    }
}
