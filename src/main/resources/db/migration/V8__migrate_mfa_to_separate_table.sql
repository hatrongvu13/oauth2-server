-- ==================================================
-- V8__migrate_mfa_to_separate_table.sql
-- Mục đích:
-- 1. Tạo bảng mfa_config đồng bộ với Entity Java
-- 2. Xóa các tàn dư từ cấu trúc cũ
-- ==================================================

-- 1. Tạo bảng mfa_config mới
CREATE TABLE mfa_config
(
    id           VARCHAR(36) NOT NULL,
    user_id      VARCHAR(36) NOT NULL,
    username     VARCHAR(255) NOT NULL, -- Theo Entity: nullable = false
    email        VARCHAR(255) NOT NULL,    -- Theo Entity: nullable = false
    secret_key   VARCHAR(255) NOT NULL,
    enabled      BOOLEAN NOT NULL DEFAULT FALSE,
    backup_codes TEXT,                  -- Chứa danh sách mã dự phòng
    created_at   TIMESTAMP NOT NULL,    -- Theo Entity: nullable = false
    verified_at  TIMESTAMP,             -- Theo Entity: LocalDateTime verifiedAt

    CONSTRAINT pk_mfa_config PRIMARY KEY (id),
    CONSTRAINT uk_mfa_config_user_id UNIQUE (user_id),
    CONSTRAINT uk_mfa_config_username UNIQUE (username),
    CONSTRAINT uk_mfa_config_email UNIQUE (email),
    CONSTRAINT fk_mfa_config_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Index để tăng tốc truy vấn theo user_id
CREATE INDEX idx_mfa_config_user_id ON mfa_config (user_id);

-- 2. Dọn dẹp cấu trúc cũ
DROP TABLE IF EXISTS user_mfa_backup_codes CASCADE;

-- 3. Xóa cột cũ trong bảng users nếu có
ALTER TABLE users DROP COLUMN IF EXISTS mfa_secret;

-- Thêm comment quản lý
COMMENT ON TABLE mfa_config IS 'Bảng lưu cấu hình Multi-Factor Authentication (MFA) - TOTP';
COMMENT ON COLUMN mfa_config.secret_key IS 'Khóa bí mật Base32 cho Google Authenticator';
COMMENT ON COLUMN mfa_config.backup_codes IS 'Chuỗi JSON hoặc danh sách mã dự phòng cách nhau bởi dấu phẩy';