package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class EmailAlreadyExistsException extends OAuth2Exception {

    public EmailAlreadyExistsException(String email) {
        super("email_already_exists", "Email already in use: " + email, 409);
    }
}
