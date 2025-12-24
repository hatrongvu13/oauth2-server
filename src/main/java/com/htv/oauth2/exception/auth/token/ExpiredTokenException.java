package com.htv.oauth2.exception.auth.token;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class ExpiredTokenException extends OAuth2Exception {

    public ExpiredTokenException(String message) {
        super("expired_token", message, 401);
    }
}
