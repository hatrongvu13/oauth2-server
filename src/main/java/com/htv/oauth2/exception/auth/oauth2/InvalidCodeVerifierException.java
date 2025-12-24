package com.htv.oauth2.exception.auth.oauth2;

public class InvalidCodeVerifierException extends OAuth2Exception {

    public InvalidCodeVerifierException(String message) {
        super("invalid_code_verifier", message, 400);
    }

    public static class InvalidRedirectUriException extends OAuth2Exception {

        public InvalidRedirectUriException(String message) {
            super("invalid_redirect_uri", message, 400);
        }
    }

    public static class InvalidScopeException extends OAuth2Exception {

        public InvalidScopeException(String message) {
            super("invalid_scope", message, 400);
        }
    }
}
