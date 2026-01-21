-- ============================================================
-- Table: categories (카테고리)
-- Description: 게시판 카테고리 (1-depth 단순 구조)
-- ============================================================
CREATE TABLE categories (
    -- Primary Key
    id                    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '카테고리 고유 식별자',

    -- Business Fields
    name                  VARCHAR(50)     NOT NULL COMMENT '카테고리명',
    description           VARCHAR(200)    NULL COMMENT '카테고리 설명',
    sort_order            INT             NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    is_active             TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성화 여부 (0: 비활성, 1: 활성)',

    -- Auditing Fields
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at            TIMESTAMP       NULL COMMENT '삭제일시 (Soft Delete)',
    created_by            BIGINT          NULL COMMENT '생성자 ID',
    updated_by            BIGINT          NULL COMMENT '수정자 ID',

    -- Constraints
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시판 카테고리';

-- Indexes
CREATE INDEX idx_categories_sort_order ON categories (sort_order);
CREATE INDEX idx_categories_is_active ON categories (is_active);
CREATE INDEX idx_categories_deleted_at ON categories (deleted_at);
