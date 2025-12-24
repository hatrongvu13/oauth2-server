package com.htv.oauth2.exception.auth.oauth2;

public class InvalidCodeChallengeException extends OAuth2Exception {

    public InvalidCodeChallengeException(String message) {
        super("invalid_code_challenge", message, 400);
    }
}
