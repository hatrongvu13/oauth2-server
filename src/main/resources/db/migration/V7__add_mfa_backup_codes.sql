-- V7__add_mfa_backup_codes.sql

-- Bảng này lưu trữ các mã dự phòng (backup codes) dùng một lần
-- cho Multi-Factor Authentication (MFA).

CREATE TABLE user_mfa_backup_codes
(
    user_id     VARCHAR(255) NOT NULL,
    backup_code VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, backup_code),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Tạo index để tăng tốc độ truy vấn
CREATE INDEX idx_user_mfa_backup_codes_user_id ON user_mfa_backup_codes (user_id);