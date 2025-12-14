-- ============================================
-- V1__create_users_table.sql
-- ============================================

CREATE TABLE users
(
    id                    VARCHAR(36) PRIMARY KEY,
    username              VARCHAR(100) UNIQUE NOT NULL,
    email                 VARCHAR(255) UNIQUE NOT NULL,
    password_hash         VARCHAR(255)        NOT NULL,
    first_name            VARCHAR(100),
    last_name             VARCHAR(100),
    enabled               BOOLEAN             NOT NULL DEFAULT true,
    email_verified        BOOLEAN             NOT NULL DEFAULT false,
    mfa_enabled           BOOLEAN             NOT NULL DEFAULT false,
    mfa_secret            VARCHAR(255),
    failed_login_attempts INTEGER                      DEFAULT 0,
    account_locked_until  TIMESTAMP,
    last_login            TIMESTAMP,
    created_at            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles
(
    user_id VARCHAR(36) NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_enabled ON users (enabled);


