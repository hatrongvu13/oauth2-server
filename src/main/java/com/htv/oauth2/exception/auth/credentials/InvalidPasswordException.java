package com.htv.oauth2.exception.auth.credentials;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InvalidPasswordException extends OAuth2Exception {

    public InvalidPasswordException(String message) {
        super("invalid_password", message, 400);
    }
}
