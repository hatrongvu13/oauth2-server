-- ============================================
-- V4__create_audit_logs_table.sql
-- ============================================

CREATE TABLE audit_logs
(
    id            VARCHAR(36) PRIMARY KEY,
    user_id       VARCHAR(36),
    action        VARCHAR(100) NOT NULL,
    resource      VARCHAR(255),
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    status        VARCHAR(20),
    error_message VARCHAR(1000),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_log_user ON audit_logs (user_id);
CREATE INDEX idx_audit_log_created ON audit_logs (created_at);
CREATE INDEX idx_audit_log_action ON audit_logs (action);
CREATE INDEX idx_audit_log_status ON audit_logs (status);
