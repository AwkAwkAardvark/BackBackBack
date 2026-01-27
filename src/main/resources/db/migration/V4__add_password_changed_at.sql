-- users 테이블에 password_changed_at 컬럼 추가
ALTER TABLE users ADD COLUMN password_changed_at DATETIME;

-- 기존 데이터는 현재 시간으로 초기화 (또는 created_at으로 초기화 가능하지만, 안전하게 현재 시간)
UPDATE users SET password_changed_at = NOW() WHERE password_changed_at IS NULL;
