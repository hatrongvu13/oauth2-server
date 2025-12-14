-- ============================================
-- V2__create_clients_table.sql
-- ============================================

CREATE TABLE oauth2_clients
(
    id                     VARCHAR(36) PRIMARY KEY,
    client_id              VARCHAR(100) UNIQUE NOT NULL,
    client_secret          VARCHAR(255)        NOT NULL,
    client_name            VARCHAR(255)        NOT NULL,
    description            VARCHAR(1000),
    access_token_validity  INTEGER             NOT NULL DEFAULT 3600,
    refresh_token_validity INTEGER             NOT NULL DEFAULT 86400,
    auto_approve           BOOLEAN             NOT NULL DEFAULT false,
    enabled                BOOLEAN             NOT NULL DEFAULT true,
    created_at             TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE client_redirect_uris
(
    client_id    VARCHAR(36)  NOT NULL,
    redirect_uri VARCHAR(500) NOT NULL,
    PRIMARY KEY (client_id, redirect_uri),
    FOREIGN KEY (client_id) REFERENCES oauth2_clients (id) ON DELETE CASCADE
);

CREATE TABLE client_grant_types
(
    client_id  VARCHAR(36) NOT NULL,
    grant_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (client_id, grant_type),
    FOREIGN KEY (client_id) REFERENCES oauth2_clients (id) ON DELETE CASCADE
);

CREATE TABLE client_scopes
(
    client_id VARCHAR(36)  NOT NULL,
    scope     VARCHAR(100) NOT NULL,
    PRIMARY KEY (client_id, scope),
    FOREIGN KEY (client_id) REFERENCES oauth2_clients (id) ON DELETE CASCADE
);

CREATE INDEX idx_client_client_id ON oauth2_clients (client_id);
CREATE INDEX idx_client_enabled ON oauth2_clients (enabled);
