package com.htv.oauth2.exception;

public class UnsupportedResponseTypeException extends OAuth2Exception {

    public UnsupportedResponseTypeException(String responseType) {
        super("unsupported_response_type", "Response type not supported: " + responseType, 400);
    }
}
