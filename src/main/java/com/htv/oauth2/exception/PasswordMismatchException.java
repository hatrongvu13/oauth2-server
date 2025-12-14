package com.htv.oauth2.exception;

public class PasswordMismatchException extends OAuth2Exception {

    public PasswordMismatchException() {
        super("password_mismatch", "Passwords do not match", 400);
    }
}
