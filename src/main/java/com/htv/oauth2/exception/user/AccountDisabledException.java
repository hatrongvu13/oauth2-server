package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class AccountDisabledException extends OAuth2Exception {

    public AccountDisabledException(String message) {
        super("account_disabled", message, 403);
    }
}
