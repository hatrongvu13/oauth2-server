package com.htv.oauth2.exception;

public class InvalidCodeVerifierException extends OAuth2Exception {

    public InvalidCodeVerifierException(String message) {
        super("invalid_code_verifier", message, 400);
    }
}
