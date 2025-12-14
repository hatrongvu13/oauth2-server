-- ============================================
-- V3__create_tokens_table.sql
-- ============================================

CREATE TABLE authorization_codes
(
    id                    VARCHAR(36) PRIMARY KEY,
    code                  VARCHAR(255) UNIQUE NOT NULL,
    client_id             VARCHAR(36)         NOT NULL,
    user_id               VARCHAR(36)         NOT NULL,
    redirect_uri          VARCHAR(500)        NOT NULL,
    code_challenge        VARCHAR(255),
    code_challenge_method VARCHAR(10),
    used                  BOOLEAN             NOT NULL DEFAULT false,
    expires_at            TIMESTAMP           NOT NULL,
    created_at            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES oauth2_clients (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE authorization_code_scopes
(
    auth_code_id VARCHAR(36)  NOT NULL,
    scope        VARCHAR(100) NOT NULL,
    PRIMARY KEY (auth_code_id, scope),
    FOREIGN KEY (auth_code_id) REFERENCES authorization_codes (id) ON DELETE CASCADE
);

CREATE TABLE access_tokens
(
    id         VARCHAR(36) PRIMARY KEY,
    token      TEXT         NOT NULL,
    client_id  VARCHAR(100) NOT NULL,
    user_id    VARCHAR(36),
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE access_token_scopes
(
    access_token_id VARCHAR(36)  NOT NULL,
    scope           VARCHAR(100) NOT NULL,
    PRIMARY KEY (access_token_id, scope),
    FOREIGN KEY (access_token_id) REFERENCES access_tokens (id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens
(
    id              VARCHAR(36) PRIMARY KEY,
    token           VARCHAR(500) UNIQUE NOT NULL,
    access_token_id VARCHAR(36)         NOT NULL,
    client_id       VARCHAR(100)        NOT NULL,
    user_id         VARCHAR(36)         NOT NULL,
    revoked         BOOLEAN             NOT NULL DEFAULT false,
    expires_at      TIMESTAMP           NOT NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (access_token_id) REFERENCES access_tokens (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_auth_code_code ON authorization_codes (code);
CREATE INDEX idx_auth_code_expires ON authorization_codes (expires_at);
CREATE INDEX idx_auth_code_user ON authorization_codes (user_id);

CREATE INDEX idx_access_token_token ON access_tokens(token);
CREATE INDEX idx_access_token_expires ON access_tokens (expires_at);
CREATE INDEX idx_access_token_user ON access_tokens (user_id);
CREATE INDEX idx_access_token_revoked ON access_tokens (revoked);

CREATE INDEX idx_refresh_token_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);
