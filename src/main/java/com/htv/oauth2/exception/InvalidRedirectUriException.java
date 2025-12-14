package com.htv.oauth2.exception;

public class InvalidRedirectUriException extends OAuth2Exception {

    public InvalidRedirectUriException(String message) {
        super("invalid_redirect_uri", message, 400);
    }
}
