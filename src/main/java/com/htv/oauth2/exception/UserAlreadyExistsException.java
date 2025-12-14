package com.htv.oauth2.exception;

public class UserAlreadyExistsException extends OAuth2Exception {

    public UserAlreadyExistsException(String message) {
        super("user_already_exists", message, 409);
    }
}
