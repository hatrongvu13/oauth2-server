package com.htv.oauth2.exception;

public class AccountDisabledException extends OAuth2Exception {

    public AccountDisabledException(String message) {
        super("account_disabled", message, 403);
    }
}
