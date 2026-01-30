# ERD 정리 (현재 적용 스키마)

이 문서는 **현재 프로젝트에 적용된 DB 스키마**를 기준으로 ERD를 요약합니다.  
DDL 원본은 아래 Flyway 마이그레이션 파일을 기준으로 관리합니다.

## 기준 마이그레이션
- MySQL: `src/main/resources/db/migration/V1__init.sql`
- H2(테스트): `src/main/resources/db/migration-h2/V1__init.sql`

## 공통 규칙
- 대부분의 테이블은 **BIGINT PK + AUTO_INCREMENT**를 사용합니다.
- 감사 컬럼(Audit): `created_at`, `updated_at`, `created_by`, `updated_by`
- 소프트 삭제: `deleted_at` 존재(일부 테이블 제외)

---

## 1) 공통 도메인

### companies
- 기업 기본 정보
- `corp_code`(UK), `stock_code`(UK)

### users
- 사용자 계정
- `uuid`(UK), `email`(UK)
- `company_id` → `companies.id` (NULL 허용)
- `status`: `PENDING`, `ACTIVE`, `INACTIVE`, `BANNED`

### roles / user_roles
- 역할 정의 및 사용자-역할 매핑
- `roles.name`: `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_USER`
- `user_roles`는 `(user_id, role_id)` UK

### refresh_tokens
- 리프레시 토큰 관리
- `user_id` → `users.id`
- `token_value`(UK)

### email_verifications
- 이메일 인증 이력
- `user_id` → `users.id`
- `token`(UK)
- `status`: `PENDING`, `VERIFIED`, `EXPIRED`

---

## 2) 게시판 도메인

### categories
- 게시판 카테고리
- `name`(UK)
- `is_active` + `deleted_at` 인덱스

### posts
- 게시글
- `user_id` → `users.id`
- `category_id` → `categories.id`
- `status`: `DRAFT`, `PUBLISHED`, `HIDDEN`

### post_view_counts
- 게시글 조회수 분리 테이블 (1:1)
- `post_id` → `posts.id`

### comments
- 댓글 및 대댓글
- `post_id` → `posts.id`
- `user_id` → `users.id`
- `parent_id` → `comments.id` (NULL 허용)

### tags / post_tags
- 태그 및 게시글-태그 매핑
- `tags.name`(UK)
- `post_tags`는 `(post_id, tag_id)` UK

### post_likes
- 게시글 좋아요 매핑
- `(post_id, user_id)` UK

---

## 3) 파일 도메인

### files
- 파일 메타데이터
- `usage_type`: `POST_ATTACHMENT`, `REPORT_PDF`

### post_files
- 게시글-파일 매핑
- `post_id` → `posts.id`
- `file_id` → `files.id`

---

## 4) 시간 차원

### quarters
- 분기 정보
- `(year, quarter)` UK
- `quarter_key`(UK)

---

## 5) 보고서 도메인

### company_reports
- 기업+분기 기준 보고서
- `company_id` → `companies.id`
- `quarter_id` → `quarters.id`
- `post_id` → `posts.id` (NULL 허용)

### company_report_versions
- 보고서 버전 이력
- `company_report_id` → `company_reports.id`
- `pdf_file_id` → `files.id` (NULL 허용)
- `(company_report_id, version_no)` UK

---

## 6) 재무 지표 도메인

### metrics
- 지표 정의 (테이블명 복수형 통일)
- `metric_code`(UK)

### company_report_metric_values
- 보고서 지표 값 (실제/예측)
- `report_version_id` → `company_report_versions.id`
- `metric_id` → `metrics.id`
- `quarter_id` → `quarters.id`
- `value_type`: `ACTUAL`, `PREDICTED`
- 복합 인덱스
  - `idx_crmv_version_quarter` (`report_version_id`, `quarter_id`)
  - `idx_crmv_metric_quarter_type` (`metric_id`, `quarter_id`, `value_type`)

---

## 관계 요약 (텍스트)

```
companies ── users ── posts ── comments
     │          │        ├── post_files ── files
     │          │        ├── post_tags ── tags
     │          │        └── post_likes
     │          ├── user_roles ── roles
     │          ├── refresh_tokens
     │          └── email_verifications
     │
     └── company_reports ── company_report_versions ── files (PDF)
                         └── quarters
                              └── company_report_metric_values ── metrics
```
