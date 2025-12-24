package com.htv.oauth2.exception.auth.token;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InvalidAuthorizationCodeException extends OAuth2Exception {

    public InvalidAuthorizationCodeException(String message) {
        super("invalid_authorization_code", message, 400);
    }
}
