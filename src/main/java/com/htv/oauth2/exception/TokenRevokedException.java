package com.htv.oauth2.exception;

public class TokenRevokedException extends OAuth2Exception {

    public TokenRevokedException(String message) {
        super("token_revoked", message, 401);
    }
}
