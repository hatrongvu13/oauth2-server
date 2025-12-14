package com.htv.oauth2.exception;

public class AccountLockedException extends OAuth2Exception {

    public AccountLockedException(String message) {
        super("account_locked", message, 403);
    }
}
