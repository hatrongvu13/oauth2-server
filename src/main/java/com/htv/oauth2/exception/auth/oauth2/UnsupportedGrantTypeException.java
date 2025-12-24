package com.htv.oauth2.exception.auth.oauth2;

public class UnsupportedGrantTypeException extends OAuth2Exception {

    public UnsupportedGrantTypeException(String grantType) {
        super("unsupported_grant_type", "Grant type not supported: " + grantType, 400);
    }
}
