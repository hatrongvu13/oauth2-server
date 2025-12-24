package com.htv.oauth2.exception.auth.token;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class TokenRevokedException extends OAuth2Exception {

    public TokenRevokedException(String message) {
        super("token_revoked", message, 401);
    }
}
