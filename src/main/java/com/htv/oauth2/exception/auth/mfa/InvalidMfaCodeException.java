package com.htv.oauth2.exception.auth.mfa;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class InvalidMfaCodeException extends OAuth2Exception {

    public InvalidMfaCodeException(String message) {
        super("invalid_mfa_code", message, 401);
    }
}
