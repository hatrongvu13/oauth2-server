package com.htv.oauth2.exception.auth.oauth2;

public class CodeChallengeMismatchException extends OAuth2Exception {

    public CodeChallengeMismatchException() {
        super("code_challenge_mismatch", "Code verifier does not match code challenge", 400);
    }
}
