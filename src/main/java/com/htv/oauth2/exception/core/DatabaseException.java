package com.htv.oauth2.exception.core;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class DatabaseException extends OAuth2Exception {

    public DatabaseException(String message, Throwable cause) {
        super("database_error", message, 500, cause);
    }
}
