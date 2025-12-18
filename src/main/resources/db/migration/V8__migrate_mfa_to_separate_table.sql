-- ==================================================
-- V8__migrate_mfa_to_separate_table.sql
-- Mục đích:
-- 1. Tạo bảng mới mfa_config để quản lý MFA độc lập
-- 2. Xóa bảng cũ user_mfa_backup_codes (không còn cần nữa)
-- 3. Xóa cột mfa_secret khỏi bảng users (nếu còn tồn tại)
-- ==================================================

-- 1. Tạo bảng mfa_config mới
CREATE TABLE mfa_config
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL UNIQUE,
    username     VARCHAR(255),          -- lưu username để hiển thị đẹp trên QR code
    email        VARCHAR(255),          -- lưu email để hiển thị đẹp trên QR code
    secret_key   VARCHAR(255) NOT NULL, -- Base32 secret từ GoogleAuthenticator
    enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    verified_at  TIMESTAMP WITH TIME ZONE,
    backup_codes TEXT,                  -- Lưu dạng "code1,code2,code3,...,code10" (dễ quản lý và regenerate)

    CONSTRAINT fk_mfa_config_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Index để tăng tốc truy vấn theo user_id (chính là UNIQUE rồi, nhưng thêm cho rõ)
CREATE INDEX idx_mfa_config_user_id ON mfa_config (user_id);

-- 2. Xóa bảng cũ user_mfa_backup_codes (nếu tồn tại)
DROP TABLE IF EXISTS user_mfa_backup_codes CASCADE;

-- 3. Xóa cột mfa_secret khỏi bảng users (nếu còn tồn tại từ phiên bản cũ)
ALTER TABLE users DROP COLUMN IF EXISTS mfa_secret;

-- Optional: Nếu bạn muốn thêm comment cho bảng và cột để dễ quản lý
COMMENT
ON TABLE mfa_config IS 'Quản lý cấu hình Multi-Factor Authentication (MFA) cho từng user';
COMMENT
ON COLUMN mfa_config.user_id IS 'Khóa ngoại tham chiếu đến users.id';
COMMENT
ON COLUMN mfa_config.secret_key IS 'Secret key dạng Base32 dùng cho TOTP (Google Authenticator)';
COMMENT
ON COLUMN mfa_config.backup_codes IS 'Các mã dự phòng dùng một lần, lưu dưới dạng chuỗi phân cách bằng dấu phẩy';
COMMENT
ON COLUMN mfa_config.enabled IS 'MFA đã được kích hoạt hoàn toàn sau khi verify code đầu tiên';
COMMENT
ON COLUMN mfa_config.verified_at IS 'Thời điểm user verify thành công code TOTP đầu tiên';