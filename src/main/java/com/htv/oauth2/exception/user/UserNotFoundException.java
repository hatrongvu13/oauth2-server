package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class UserNotFoundException extends OAuth2Exception {

    public UserNotFoundException(String identifier) {
        super("user_not_found", "User not found: " + identifier, 404);
    }
}
