package com.htv.oauth2.exception;

public class InvalidCodeChallengeException extends OAuth2Exception {

    public InvalidCodeChallengeException(String message) {
        super("invalid_code_challenge", message, 400);
    }
}
