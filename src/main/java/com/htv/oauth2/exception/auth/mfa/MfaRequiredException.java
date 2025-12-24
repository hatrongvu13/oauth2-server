package com.htv.oauth2.exception.auth.mfa;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class MfaRequiredException extends OAuth2Exception {

    private final String mfaToken;

    public MfaRequiredException(String message, String mfaToken) {
        super("mfa_required", message, 401);
        this.mfaToken = mfaToken;
    }

    public String getMfaToken() {
        return mfaToken;
    }
}
