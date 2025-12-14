package com.htv.oauth2.exception;

public class DatabaseException extends OAuth2Exception {

    public DatabaseException(String message, Throwable cause) {
        super("database_error", message, 500, cause);
    }
}
