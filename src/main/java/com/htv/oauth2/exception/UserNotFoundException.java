package com.htv.oauth2.exception;

public class UserNotFoundException extends OAuth2Exception {

    public UserNotFoundException(String identifier) {
        super("user_not_found", "User not found: " + identifier, 404);
    }
}
