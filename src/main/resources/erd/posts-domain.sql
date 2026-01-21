-- ============================================================
-- Table: posts (게시글)
-- Description: 게시판 게시글 정보
-- ============================================================
CREATE TABLE posts (
    -- Primary Key
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '게시글 고유 식별자',

    -- Foreign Keys
    user_id               BIGINT          NOT NULL COMMENT '작성자 ID',
    category_id           BIGINT          NOT NULL COMMENT '카테고리 ID',

    -- Content Fields
    title                 VARCHAR(200)    NOT NULL COMMENT '게시글 제목',
    content               LONGTEXT        NOT NULL COMMENT '게시글 내용',

    -- Statistics
    view_count            INT             NOT NULL DEFAULT 0 COMMENT '조회수',

    -- Flags
    is_pinned             TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '공지 여부 (0: 일반, 1: 공지)',
    status                VARCHAR(20)     NOT NULL DEFAULT 'PUBLISHED' COMMENT '게시 상태 (DRAFT, PUBLISHED, HIDDEN)',

    -- Auditing Fields
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at            TIMESTAMP       NULL COMMENT '삭제일시 (Soft Delete)',
    created_by            BIGINT          NULL COMMENT '생성자 ID',
    updated_by            BIGINT          NULL COMMENT '수정자 ID',

    -- Constraints
    PRIMARY KEY (id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글';

-- Indexes
CREATE INDEX idx_posts_user_id ON posts (user_id);
CREATE INDEX idx_posts_category_id ON posts (category_id);
CREATE INDEX idx_posts_status ON posts (status);
CREATE INDEX idx_posts_is_pinned ON posts (is_pinned);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX idx_posts_deleted_at ON posts (deleted_at);
-- 복합 인덱스: 카테고리별 최신 게시글 조회 최적화
CREATE INDEX idx_posts_category_created ON posts (category_id, created_at DESC);
-- 복합 인덱스: 공지사항 우선 정렬
CREATE INDEX idx_posts_pinned_created ON posts (is_pinned DESC, created_at DESC);
