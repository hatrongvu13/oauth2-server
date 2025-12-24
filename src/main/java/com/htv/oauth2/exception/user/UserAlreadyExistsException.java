package com.htv.oauth2.exception.user;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class UserAlreadyExistsException extends OAuth2Exception {

    public UserAlreadyExistsException(String message) {
        super("user_already_exists", message, 409);
    }
}
