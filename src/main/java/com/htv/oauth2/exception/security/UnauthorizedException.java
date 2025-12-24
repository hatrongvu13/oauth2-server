package com.htv.oauth2.exception.security;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class UnauthorizedException extends OAuth2Exception {
    public UnauthorizedException(String message) {
        super("unauthorized", message, 401);
    }
}
