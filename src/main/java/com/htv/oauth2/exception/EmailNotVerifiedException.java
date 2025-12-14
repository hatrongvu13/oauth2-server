package com.htv.oauth2.exception;

public class EmailNotVerifiedException extends OAuth2Exception {

    public EmailNotVerifiedException(String message) {
        super("email_not_verified", message, 403);
    }
}
