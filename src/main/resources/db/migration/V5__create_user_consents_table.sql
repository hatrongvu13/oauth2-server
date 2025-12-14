-- ============================================
-- V5__create_user_consents_table.sql
-- ============================================

CREATE TABLE user_consents
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    client_id  VARCHAR(36) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES oauth2_clients (id) ON DELETE CASCADE,
    UNIQUE (user_id, client_id)
);

CREATE TABLE user_consent_scopes
(
    consent_id VARCHAR(36)  NOT NULL,
    scope      VARCHAR(100) NOT NULL,
    PRIMARY KEY (consent_id, scope),
    FOREIGN KEY (consent_id) REFERENCES user_consents (id) ON DELETE CASCADE
);

CREATE INDEX idx_consent_user_client ON user_consents (user_id, client_id);
