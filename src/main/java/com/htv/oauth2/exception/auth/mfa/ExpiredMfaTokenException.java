package com.htv.oauth2.exception.auth.mfa;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class ExpiredMfaTokenException extends OAuth2Exception {
    public ExpiredMfaTokenException(String message) {
        super("mfa_token", message, 410);
    }
}
