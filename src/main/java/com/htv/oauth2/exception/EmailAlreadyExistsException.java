package com.htv.oauth2.exception;

public class EmailAlreadyExistsException extends OAuth2Exception {

    public EmailAlreadyExistsException(String email) {
        super("email_already_exists", "Email already in use: " + email, 409);
    }
}
