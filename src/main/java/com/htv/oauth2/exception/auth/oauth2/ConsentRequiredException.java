package com.htv.oauth2.exception.auth.oauth2;

public class ConsentRequiredException extends OAuth2Exception {

    public ConsentRequiredException(String message) {
        super("consent_required", message, 403);
    }
}
