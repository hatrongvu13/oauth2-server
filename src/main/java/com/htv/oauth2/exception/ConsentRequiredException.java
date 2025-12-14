package com.htv.oauth2.exception;

public class ConsentRequiredException extends OAuth2Exception {

    public ConsentRequiredException(String message) {
        super("consent_required", message, 403);
    }
}
