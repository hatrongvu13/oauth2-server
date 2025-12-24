package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class AccountLockedException extends OAuth2Exception {

    public AccountLockedException(String message) {
        super("account_locked", message, 403);
    }
}
