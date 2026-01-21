-- ============================================================
-- Table: tags (태그)
-- Description: 게시글 태깅 시스템
-- ============================================================
CREATE TABLE tags (
    -- Primary Key
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '태그 고유 식별자',

    -- Business Fields
    name                  VARCHAR(50)     NOT NULL COMMENT '태그명',

    -- Auditing Fields
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at            TIMESTAMP       NULL COMMENT '삭제일시 (Soft Delete)',
    created_by            BIGINT          NULL COMMENT '생성자 ID',
    updated_by            BIGINT          NULL COMMENT '수정자 ID',

    -- Constraints
    PRIMARY KEY (id),
    CONSTRAINT uk_tags_name UNIQUE (name, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='태그';

-- Index
CREATE INDEX idx_tags_deleted_at ON tags (deleted_at);

-- ============================================================
-- Table: post_tags (게시글-태그 매핑)
-- Description: 게시글과 태그의 N:M 관계 매핑
-- ============================================================
CREATE TABLE post_tags (
    -- Primary Key
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 고유 식별자',

    -- Foreign Keys
    post_id               BIGINT          NOT NULL COMMENT '게시글 ID',
    tag_id                BIGINT          NOT NULL COMMENT '태그 ID',

    -- Auditing
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    -- Constraints
    PRIMARY KEY (id),
    CONSTRAINT uk_post_tags UNIQUE (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글-태그 매핑';

-- Indexes
CREATE INDEX idx_post_tags_post_id ON post_tags (post_id);
CREATE INDEX idx_post_tags_tag_id ON post_tags (tag_id);
