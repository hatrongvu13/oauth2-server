package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class UsernameAlreadyExistsException extends OAuth2Exception {

    public UsernameAlreadyExistsException(String username) {
        super("username_already_exists", "Username already taken: " + username, 409);
    }
}
