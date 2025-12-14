package com.htv.oauth2.exception;

public class AccessDeniedException extends OAuth2Exception {

    public AccessDeniedException(String message) {
        super("access_denied", message, 403);
    }
}
