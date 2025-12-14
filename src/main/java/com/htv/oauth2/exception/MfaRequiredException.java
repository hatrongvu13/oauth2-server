package com.htv.oauth2.exception;

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
