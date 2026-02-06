-- ============================================
-- 1. 공통 도메인
-- ============================================

CREATE TABLE `companies` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '기업 고유 식별자',
                             `corp_code` CHAR(8) NOT NULL COMMENT '기업 고유번호 (DART)',
                             `corp_name` VARCHAR(100) NOT NULL COMMENT '회사명',
                             `corp_eng_name` VARCHAR(200) NULL COMMENT '영문 회사명',
                             `stock_code` CHAR(6) NULL COMMENT '주식 종목코드',
                             `modify_date` DATE NULL COMMENT '최종 수정일',
                             `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                             `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                             `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                             `created_by` BIGINT NULL COMMENT '생성자 ID',
                             `updated_by` BIGINT NULL COMMENT '수정자 ID',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_corp_code` (`corp_code`),
                             UNIQUE KEY `uk_stock_code` (`stock_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='기업 정보';

CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 식별자',
                         `company_id` BIGINT NULL COMMENT '소속 회사 ID',
                         `uuid` BINARY(16) NOT NULL COMMENT '외부 API 노출용 UUID',
                         `email` VARCHAR(100) NOT NULL COMMENT '이메일 (로그인 ID)',
                         `password` VARCHAR(255) NOT NULL COMMENT '비밀번호 (BCrypt 해시)',
                         `password_changed_at` DATETIME NULL COMMENT '비밀번호 변경 시각',
                         `name` VARCHAR(50) NOT NULL COMMENT '사용자 이름',
                         `phone` VARCHAR(20) NULL COMMENT '연락처',
                         `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '계정 상태 (PENDING, ACTIVE, INACTIVE, BANNED)',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                         `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                         `created_by` BIGINT NULL COMMENT '생성자 ID',
                         `updated_by` BIGINT NULL COMMENT '수정자 ID',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_users_uuid` (`uuid`),
                         UNIQUE KEY `uk_users_email` (`email`),
                         INDEX `idx_users_company` (`company_id`),
                         INDEX `idx_users_status` (`status`),
                         CONSTRAINT `fk_users_company`
                             FOREIGN KEY (`company_id`) REFERENCES `companies`(`id`)
                                 ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자';

CREATE TABLE `roles` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '역할 고유 식별자',
                         `name` VARCHAR(50) NOT NULL COMMENT '역할명 (ROLE_USER, ROLE_ADMIN, ROLE_ANALYST)',
                         `description` VARCHAR(200) NULL COMMENT '역할 설명',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                         `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                         `created_by` BIGINT NULL COMMENT '생성자 ID',
                         `updated_by` BIGINT NULL COMMENT '수정자 ID',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='역할 정의';

CREATE TABLE `user_roles` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '매핑 고유 식별자',
                              `user_id` BIGINT NOT NULL COMMENT '사용자 고유 식별자',
                              `role_id` BIGINT NOT NULL COMMENT '역할 ID',
                              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
                              INDEX `idx_ur_role` (`role_id`),
                              CONSTRAINT `fk_ur_user`
                                  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
                                      ON DELETE CASCADE,
                              CONSTRAINT `fk_ur_role`
                                  FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 역할 매핑';

CREATE TABLE `refresh_tokens` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '토큰 고유 식별자',
                                  `user_id` BIGINT NOT NULL COMMENT '사용자 고유 식별자',
                                  `token_value` VARCHAR(512) NOT NULL COMMENT '리프레시 토큰 값',
                                  `device_info` VARCHAR(500) NULL COMMENT '디바이스 정보 (User-Agent)',
                                  `ip_address` VARCHAR(45) NULL COMMENT 'IP 주소 (IPv6 지원)',
                                  `expires_at` TIMESTAMP NOT NULL COMMENT '토큰 만료일시',
                                  `is_revoked` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '폐기 여부 (0: 유효, 1: 폐기)',
                                  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_token_value` (`token_value`),
                                  INDEX `idx_rt_user` (`user_id`),
                                  INDEX `idx_rt_expires` (`expires_at`),
                                  CONSTRAINT `fk_rt_user`
                                      FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
                                          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='리프레시 토큰';

CREATE TABLE `email_verifications` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '이메일 인증 고유 식별자',
                                       `user_id` BIGINT NOT NULL COMMENT '사용자 고유 식별자',
                                       `email` VARCHAR(100) NOT NULL COMMENT '인증 이메일',
                                       `token` VARCHAR(255) NOT NULL COMMENT '인증 토큰',
                                       `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING, VERIFIED, EXPIRED)',
                                       `expired_at` TIMESTAMP NOT NULL COMMENT '만료 일시',
                                       `verified_at` TIMESTAMP NULL COMMENT '인증 완료 일시',
                                       `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                       `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                       `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                                       `created_by` BIGINT NULL COMMENT '생성자 ID',
                                       `updated_by` BIGINT NULL COMMENT '수정자 ID',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_ev_token` (`token`),
                                       INDEX `idx_ev_user` (`user_id`),
                                       INDEX `idx_ev_email` (`email`),
                                       INDEX `idx_ev_status_expired` (`status`, `expired_at`),
                                       CONSTRAINT `fk_ev_user`
                                           FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
                                               ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이메일 인증';

-- ============================================
-- 2. 게시판 도메인
-- ============================================

CREATE TABLE `categories` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '카테고리 고유 식별자',
                              `name` VARCHAR(50) NOT NULL COMMENT '카테고리명',
                              `description` VARCHAR(200) NULL COMMENT '카테고리 설명',
                              `sort_order` INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
                              `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '활성화 여부 (0: 비활성, 1: 활성)',
                              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                              `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                              `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                              `created_by` BIGINT NULL COMMENT '생성자 ID',
                              `updated_by` BIGINT NULL COMMENT '수정자 ID',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_category_name` (`name`),
                              INDEX `idx_categories_active` (`is_active`, `deleted_at`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시판 카테고리';

CREATE TABLE `posts` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '게시글 고유 식별자',
                         `user_id` BIGINT NOT NULL COMMENT '사용자 고유 식별자',
                         `category_id` BIGINT NOT NULL COMMENT '카테고리 고유 식별자',
                         `title` VARCHAR(200) NOT NULL COMMENT '게시글 제목',
                         `content` LONGTEXT NOT NULL COMMENT '게시글 내용',
                         `is_pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '공지 여부 (0: 일반, 1: 공지)',
                         `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '게시 상태 (DRAFT, PUBLISHED, HIDDEN)',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                         `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                         `created_by` BIGINT NULL COMMENT '생성자 ID',
                         `updated_by` BIGINT NULL COMMENT '수정자 ID',
                         PRIMARY KEY (`id`),
                         INDEX `idx_posts_user` (`user_id`),
                         INDEX `idx_posts_category_created` (`category_id`, `deleted_at`, `created_at` DESC),
                         INDEX `idx_posts_status` (`status`, `is_pinned`),
                         CONSTRAINT `fk_posts_user`
                             FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
                                 ON DELETE CASCADE,
                         CONSTRAINT `fk_posts_category`
                             FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
                                 ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글';

CREATE TABLE `post_view_counts` (
                                    `post_id` BIGINT NOT NULL COMMENT '게시글 고유 식별자',
                                    `view_count` INT NOT NULL DEFAULT 0 COMMENT '조회수',
                                    PRIMARY KEY (`post_id`),
                                    CONSTRAINT `fk_pvc_post`
                                        FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
                                            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 조회수';

CREATE TABLE `comments` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '댓글 고유 식별자',
                            `parent_id` BIGINT NULL COMMENT '부모 댓글 ID (NULL이면 최상위 댓글)',
                            `post_id` BIGINT NOT NULL COMMENT '게시글 고유 식별자',
                            `user_id` BIGINT NOT NULL COMMENT '사용자 고유 식별자',
                            `content` TEXT NOT NULL COMMENT '댓글 내용',
                            `depth` INT NOT NULL DEFAULT 0 COMMENT '댓글 깊이 (0: 최상위)',
                            `sequence` INT NOT NULL DEFAULT 0 COMMENT '동일 부모 내 정렬 순서',
                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                            `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                            `created_by` BIGINT NULL COMMENT '생성자 ID',
                            `updated_by` BIGINT NULL COMMENT '수정자 ID',
                            PRIMARY KEY (`id`),
                            INDEX `idx_comments_post` (`post_id`, `deleted_at`, `created_at`),
                            INDEX `idx_comments_parent` (`parent_id`),
                            INDEX `idx_comments_user` (`user_id`),
                            CONSTRAINT `fk_comments_parent`
                                FOREIGN KEY (`parent_id`) REFERENCES `comments`(`id`)
                                    ON DELETE CASCADE,
                            CONSTRAINT `fk_comments_post`
                                FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
                                    ON DELETE CASCADE,
                            CONSTRAINT `fk_comments_user`
                                FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
                                    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='댓글';

CREATE TABLE `tags` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '태그 고유 식별자',
                        `name` VARCHAR(50) NOT NULL COMMENT '태그명',
                        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                        `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                        `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                        `created_by` BIGINT NULL COMMENT '생성자 ID',
                        `updated_by` BIGINT NULL COMMENT '수정자 ID',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='태그';

CREATE TABLE `post_tags` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '매핑 고유 식별자',
                             `post_id` BIGINT NOT NULL COMMENT '게시글 고유 식별자',
                             `tag_id` BIGINT NOT NULL COMMENT '태그 ID',
                             `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
                             INDEX `idx_pt_tag` (`tag_id`),
                             CONSTRAINT `fk_pt_post`
                                 FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
                                     ON DELETE CASCADE,
                             CONSTRAINT `fk_pt_tag`
                                 FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`)
                                     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 태그 매핑';

CREATE TABLE `post_likes` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '좋아요 고유 식별자',
                              `post_id` BIGINT NOT NULL COMMENT '게시글 고유 식별자',
                              `user_id` BIGINT NOT NULL COMMENT '사용자 고유 식별자',
                              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_post_like` (`post_id`, `user_id`),
                              INDEX `idx_pl_user` (`user_id`),
                              CONSTRAINT `fk_pl_post`
                                  FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
                                      ON DELETE CASCADE,
                              CONSTRAINT `fk_pl_user`
                                  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 좋아요';

-- ============================================
-- 3. 파일 도메인
-- ============================================

CREATE TABLE `files` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '파일 고유 식별자',
                         `usage_type` VARCHAR(20) NOT NULL COMMENT '사용 용도 (POST_ATTACHMENT, REPORT_PDF)',
                         `storage_url` VARCHAR(500) NOT NULL COMMENT '저장소 URL (S3 등)',
                         `original_filename` VARCHAR(255) NOT NULL COMMENT '원본 파일명',
                         `file_size` BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
                         `content_type` VARCHAR(100) NOT NULL COMMENT 'MIME 타입 (예: application/pdf)',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                         `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                         `created_by` BIGINT NULL COMMENT '생성자 ID',
                         `updated_by` BIGINT NULL COMMENT '수정자 ID',
                         PRIMARY KEY (`id`),
                         INDEX `idx_files_usage_type` (`usage_type`, `deleted_at`),
                         INDEX `idx_files_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='파일 메타데이터';

CREATE TABLE `post_files` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '매핑 고유 식별자',
                              `post_id` BIGINT NOT NULL COMMENT '게시글 고유 식별자',
                              `file_id` BIGINT NOT NULL COMMENT '파일 고유 식별자',
                              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                              PRIMARY KEY (`id`),
                              INDEX `idx_pf_post` (`post_id`),
                              INDEX `idx_pf_file` (`file_id`),
                              CONSTRAINT `fk_pf_post`
                                  FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
                                      ON DELETE CASCADE,
                              CONSTRAINT `fk_pf_file`
                                  FOREIGN KEY (`file_id`) REFERENCES `files`(`id`)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 첨부파일 매핑';

-- ============================================
-- 4. 시간 차원
-- ============================================

CREATE TABLE `quarters` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '분기 고유 식별자',
                            `year` SMALLINT NOT NULL COMMENT '연도 (2024)',
                            `quarter` TINYINT NOT NULL COMMENT '분기 (1~4)',
                            `quarter_key` INT NOT NULL COMMENT '쿼리 최적화용 키 (20241, 20242...)',
                            `start_date` DATE NOT NULL COMMENT '분기 시작일',
                            `end_date` DATE NOT NULL COMMENT '분기 종료일',
                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                            `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                            `created_by` BIGINT NULL COMMENT '생성자 ID',
                            `updated_by` BIGINT NULL COMMENT '수정자 ID',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_year_quarter` (`year`, `quarter`),
                            UNIQUE KEY `uk_quarter_key` (`quarter_key`),
                            INDEX `idx_quarters_date_range` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='분기 정보';

-- ============================================
-- 5. 보고서 도메인
-- ============================================

CREATE TABLE `company_reports` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '보고서 고유 식별자',
                                   `company_id` BIGINT NOT NULL COMMENT '기업 고유 식별자',
                                   `quarter_id` BIGINT NOT NULL COMMENT '분기 고유 식별자',
                                   `post_id` BIGINT NULL COMMENT '연결된 게시글 ID (선택사항)',
                                   `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                   `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                   `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                                   `created_by` BIGINT NULL COMMENT '생성자 ID',
                                   `updated_by` BIGINT NULL COMMENT '수정자 ID',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_company_quarter` (`company_id`, `quarter_id`),
                                   INDEX `idx_cr_company` (`company_id`),
                                   INDEX `idx_cr_quarter` (`quarter_id`),
                                   INDEX `idx_cr_post` (`post_id`),
                                   CONSTRAINT `fk_cr_company`
                                       FOREIGN KEY (`company_id`) REFERENCES `companies`(`id`)
                                           ON DELETE CASCADE,
                                   CONSTRAINT `fk_cr_quarter`
                                       FOREIGN KEY (`quarter_id`) REFERENCES `quarters`(`id`)
                                           ON DELETE CASCADE,
                                   CONSTRAINT `fk_cr_post`
                                       FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
                                           ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='기업 분기 보고서';

CREATE TABLE `company_report_versions` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '버전 고유 식별자',
                                           `company_report_id` BIGINT NOT NULL COMMENT '보고서 고유 식별자',
                                           `pdf_file_id` BIGINT NULL COMMENT '원본 PDF 파일 ID',
                                           `version_no` INT NOT NULL COMMENT '버전 번호 (1, 2, 3...)',
                                           `generated_at` DATETIME NOT NULL COMMENT '생성 일시',
                                           `is_published` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '공개 여부 (0: 비공개, 1: 공개)',
                                           `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                           `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                           `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                                           `created_by` BIGINT NULL COMMENT '생성자 ID',
                                           `updated_by` BIGINT NULL COMMENT '수정자 ID',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `uk_report_version` (`company_report_id`, `version_no`),
                                           INDEX `idx_crv_published` (`is_published`, `generated_at` DESC),
                                           INDEX `idx_crv_pdf_file` (`pdf_file_id`),
                                           CONSTRAINT `fk_crv_report`
                                               FOREIGN KEY (`company_report_id`) REFERENCES `company_reports`(`id`)
                                                   ON DELETE CASCADE,
                                           CONSTRAINT `fk_crv_file`
                                               FOREIGN KEY (`pdf_file_id`) REFERENCES `files`(`id`)
                                                   ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='보고서 버전 이력';

-- ============================================
-- 6. 재무 지표 도메인
-- ============================================

CREATE TABLE `metrics` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '지표 고유 식별자',
                           `metric_code` VARCHAR(50) NOT NULL COMMENT '지표 코드 (REVENUE, NET_INCOME...)',
                           `metric_name_ko` VARCHAR(100) NOT NULL COMMENT '지표명 (한글)',
                           `metric_name_en` VARCHAR(100) NULL COMMENT '지표명 (영문)',
                           `unit` VARCHAR(20) NULL COMMENT '단위 (원, %, 배)',
                           `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                           `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                           `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                           `created_by` BIGINT NULL COMMENT '생성자 ID',
                           `updated_by` BIGINT NULL COMMENT '수정자 ID',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_metric_code` (`metric_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='재무 지표 정의';

CREATE TABLE `company_report_metric_values` (
                                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '지표 값 고유 식별자',
                                                `report_version_id` BIGINT NOT NULL COMMENT '보고서 버전 고유 식별자',
                                                `metric_id` BIGINT NOT NULL COMMENT '지표 고유 식별자',
                                                `quarter_id` BIGINT NOT NULL COMMENT '분기 고유 식별자',
                                                `metric_value` DECIMAL(20,4) NULL COMMENT '지표 값',
                                                `value_type` VARCHAR(20) NOT NULL DEFAULT 'ACTUAL' COMMENT '값 유형 (ACTUAL=실제값, PREDICTED=예측값)',
                                                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                                `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                                                `deleted_at` TIMESTAMP NULL COMMENT '삭제일시 (Soft Delete)',
                                                `created_by` BIGINT NULL COMMENT '생성자 ID',
                                                `updated_by` BIGINT NULL COMMENT '수정자 ID',
                                                PRIMARY KEY (`id`),
                                                UNIQUE KEY `uk_metric_value_type` (`report_version_id`, `metric_id`, `quarter_id`, `value_type`),
                                                INDEX `idx_crmv_version_quarter` (`report_version_id`, `quarter_id`),
                                                INDEX `idx_crmv_metric_quarter_type` (`metric_id`, `quarter_id`, `value_type`),
                                                CONSTRAINT `fk_crmv_version`
                                                    FOREIGN KEY (`report_version_id`)
                                                        REFERENCES `company_report_versions`(`id`)
                                                        ON DELETE CASCADE,
                                                CONSTRAINT `fk_crmv_metric`
                                                    FOREIGN KEY (`metric_id`)
                                                        REFERENCES `metrics`(`id`)
                                                        ON DELETE CASCADE,
                                                CONSTRAINT `fk_crmv_quarter`
                                                    FOREIGN KEY (`quarter_id`)
                                                        REFERENCES `quarters`(`id`)
                                                        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='보고서 지표 값 (실제+예측)';
