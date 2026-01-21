-- ============================================================
-- Table: post_likes (게시글 좋아요)
-- Description: 사용자의 게시글 좋아요 기록 (중복 방지)
-- ============================================================
CREATE TABLE post_likes (
    -- Primary Key
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '좋아요 고유 식별자',

    -- Foreign Keys
    post_id               BIGINT          NOT NULL COMMENT '게시글 ID',
    user_id               BIGINT          NOT NULL COMMENT '사용자 ID',

    -- Auditing (좋아요는 생성만, 삭제는 Hard Delete)
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    -- Constraints
    PRIMARY KEY (id),
    CONSTRAINT uk_post_likes UNIQUE (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글 좋아요';

-- Indexes
CREATE INDEX idx_post_likes_post_id ON post_likes (post_id);
CREATE INDEX idx_post_likes_user_id ON post_likes (user_id);
