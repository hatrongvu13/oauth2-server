-- ============================================
-- V6__insert_default_data.sql
-- ============================================

-- Insert default admin user (password: P@ssword123)
-- BCrypt hash for "P@ssword123"
INSERT INTO users (id, username, email, password_hash, enabled, email_verified, created_at, updated_at)
VALUES ('admin-user-id',
        'admin',
        'admin@example.com',
        '$2a$10$4YTEEQWGd0W7msYPjV.Fn.Akof8at4vGNYPsgRgUgfdS1kM.qKNby',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role)
VALUES ('admin-user-id', 'ADMIN');
INSERT INTO user_roles (user_id, role)
VALUES ('admin-user-id', 'USER');

-- Insert default OAuth2 client for testing
INSERT INTO oauth2_clients (id,
                            client_id,
                            client_secret,
                            client_name,
                            description,
                            access_token_validity,
                            refresh_token_validity,
                            auto_approve,
                            enabled,
                            created_at,
                            updated_at)
VALUES ('default-client-id',
        'default-client',
        'default-secret',
        'Default Test Client',
        'Default OAuth2 client for development and testing',
        3600,
        86400,
        false,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO client_redirect_uris (client_id, redirect_uri)
VALUES ('default-client-id', 'http://localhost:8080/callback');

INSERT INTO client_grant_types (client_id, grant_type)
VALUES ('default-client-id', 'authorization_code'),
       ('default-client-id', 'refresh_token'),
       ('default-client-id', 'password');

INSERT INTO client_scopes (client_id, scope)
VALUES ('default-client-id', 'read'),
       ('default-client-id', 'write'),
       ('default-client-id', 'profile');