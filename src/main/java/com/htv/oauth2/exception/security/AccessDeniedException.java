package com.htv.oauth2.exception.security;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class AccessDeniedException extends OAuth2Exception {

    public AccessDeniedException(String message) {
        super("access_denied", message, 403);
    }
}
