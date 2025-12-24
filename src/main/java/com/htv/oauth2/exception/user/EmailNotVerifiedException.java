package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class EmailNotVerifiedException extends OAuth2Exception {

    public EmailNotVerifiedException(String message) {
        super("email_not_verified", message, 403);
    }
}
