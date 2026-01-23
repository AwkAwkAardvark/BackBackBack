-- 이메일 인증 테이블 생성
CREATE TABLE email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    token VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expired_at DATETIME NOT NULL,
    verified_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    INDEX idx_email_verifications_user_id (user_id),
    INDEX idx_email_verifications_token (token),
    INDEX idx_email_verifications_email_status (email, status),
    INDEX idx_email_verifications_expired_at (expired_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 기존 사용자 데이터를 ACTIVE 상태로 마이그레이션 (이메일 인증 없이)
UPDATE users SET status = 'ACTIVE' WHERE status = 'PENDING';
