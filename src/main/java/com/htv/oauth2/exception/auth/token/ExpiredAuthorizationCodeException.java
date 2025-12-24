package com.htv.oauth2.exception.auth.token;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class ExpiredAuthorizationCodeException extends OAuth2Exception {

    public ExpiredAuthorizationCodeException(String message) {
        super("expired_authorization_code", message, 400);
    }
}
