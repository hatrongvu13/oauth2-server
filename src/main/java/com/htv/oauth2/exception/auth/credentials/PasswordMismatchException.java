package com.htv.oauth2.exception.auth.credentials;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class PasswordMismatchException extends OAuth2Exception {

    public PasswordMismatchException() {
        super("password_mismatch", "Passwords do not match", 400);
    }
}
