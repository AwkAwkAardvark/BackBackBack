-- H2 전용 초기 스키마 (MySQL 전용 문법 제거)

CREATE TABLE industry_codes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  industry_code VARCHAR(5) NOT NULL,
  industry_name VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_industry_code UNIQUE (industry_code)
);

CREATE TABLE companies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  corp_code CHAR(8) NOT NULL,
  corp_name VARCHAR(100) NOT NULL,
  corp_eng_name VARCHAR(200),
  stock_code CHAR(6),
  industry_code VARCHAR(5),
  modify_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_corp_code UNIQUE (corp_code),
  CONSTRAINT uk_stock_code UNIQUE (stock_code),
  CONSTRAINT fk_companies_industry_code FOREIGN KEY (industry_code) REFERENCES industry_codes(industry_code)
);

CREATE INDEX idx_companies_industry_code ON companies(industry_code);

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT,
  uuid BINARY(16) NOT NULL,
  email VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  password_changed_at TIMESTAMP,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_users_uuid UNIQUE (uuid),
  CONSTRAINT uk_users_email UNIQUE (email),
  CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE user_roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_user_role UNIQUE (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_value VARCHAR(512) NOT NULL,
  device_info VARCHAR(500),
  ip_address VARCHAR(45),
  expires_at TIMESTAMP NOT NULL,
  is_revoked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_token_value UNIQUE (token_value),
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE email_verifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  email VARCHAR(100) NOT NULL,
  token VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  expired_at TIMESTAMP NOT NULL,
  verified_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_ev_token UNIQUE (token),
  CONSTRAINT fk_ev_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  sort_order INT DEFAULT 0,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content CLOB NOT NULL,
  is_pinned BOOLEAN DEFAULT FALSE,
  status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE post_view_counts (
  post_id BIGINT PRIMARY KEY,
  view_count INT DEFAULT 0,
  CONSTRAINT fk_pvc_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content CLOB NOT NULL,
  depth INT DEFAULT 0,
  sequence INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_tag_name UNIQUE (name)
);

CREATE TABLE post_tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_post_tag UNIQUE (post_id, tag_id),
  CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE TABLE post_likes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_post_like UNIQUE (post_id, user_id),
  CONSTRAINT fk_pl_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE files (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usage_type VARCHAR(20) NOT NULL,
  storage_url VARCHAR(500) NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT
);

CREATE TABLE post_files (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pf_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_pf_file FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);

CREATE TABLE quarters (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  year SMALLINT NOT NULL,
  quarter TINYINT NOT NULL,
  quarter_key INT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_year_quarter UNIQUE (year, quarter),
  CONSTRAINT uk_quarter_key UNIQUE (quarter_key)
);

CREATE TABLE company_reports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  quarter_id BIGINT NOT NULL,
  post_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_company_quarter UNIQUE (company_id, quarter_id),
  CONSTRAINT fk_cr_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_cr_quarter FOREIGN KEY (quarter_id) REFERENCES quarters(id) ON DELETE CASCADE,
  CONSTRAINT fk_cr_post FOREIGN KEY (post_id) REFERENCES posts(id)
);

CREATE TABLE company_report_versions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_report_id BIGINT NOT NULL,
  pdf_file_id BIGINT,
  version_no INT NOT NULL,
  generated_at TIMESTAMP NOT NULL,
  is_published BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_report_version UNIQUE (company_report_id, version_no),
  CONSTRAINT fk_crv_report FOREIGN KEY (company_report_id) REFERENCES company_reports(id) ON DELETE CASCADE,
  CONSTRAINT fk_crv_file FOREIGN KEY (pdf_file_id) REFERENCES files(id)
);

CREATE TABLE metrics (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  metric_code VARCHAR(50) NOT NULL,
  metric_name_ko VARCHAR(100) NOT NULL,
  metric_name_en VARCHAR(100),
  unit VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_metric_code UNIQUE (metric_code)
);

CREATE TABLE company_report_metric_values (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_version_id BIGINT NOT NULL,
  metric_id BIGINT NOT NULL,
  quarter_id BIGINT NOT NULL,
  metric_value DECIMAL(20,4),
  value_type VARCHAR(20) DEFAULT 'ACTUAL',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  CONSTRAINT uk_metric_value_type UNIQUE (report_version_id, metric_id, quarter_id, value_type),
  CONSTRAINT fk_crmv_version FOREIGN KEY (report_version_id) REFERENCES company_report_versions(id) ON DELETE CASCADE,
  CONSTRAINT fk_crmv_metric FOREIGN KEY (metric_id) REFERENCES metrics(id) ON DELETE CASCADE,
  CONSTRAINT fk_crmv_quarter FOREIGN KEY (quarter_id) REFERENCES quarters(id) ON DELETE CASCADE
);

-- ============================================
-- 7. 업종 코드 초기 데이터
-- ============================================

MERGE INTO industry_codes (industry_code, industry_name)
KEY (industry_code)
VALUES
  ('11', '작물 재배업'),
  ('26', '전자 부품, 컴퓨터, 영상, 음향 및 통신장비 제조업'),
  ('31', '어로 어업'),
  ('46', '도매 및 상품 중개업'),
  ('101', '도축, 육류 가공 및 저장 처리업'),
  ('102', '수산물 가공 및 저장 처리업'),
  ('103', '과실, 채소 가공 및 저장 처리업'),
  ('104', '동물성 및 식물성 유지 제조업'),
  ('105', '낙농제품 및 식용 빙과류 제조업'),
  ('107', '기타 식품 제조업'),
  ('108', '동물용 사료 및 조제식품 제조업'),
  ('111', '알코올 음료 제조업'),
  ('112', '비알코올 음료 및 얼음 제조업'),
  ('120', '담배 제조업'),
  ('131', '방적 및 가공사 제조업'),
  ('132', '직물 직조 및 직물제품 제조업'),
  ('133', '편조 원단 제조업'),
  ('139', '기타 섬유제품 제조업'),
  ('141', '봉제의복 제조업'),
  ('143', '편조의복 제조업'),
  ('144', '의복 액세서리 제조업'),
  ('151', '가죽, 가방 및 유사 제품 제조업'),
  ('152', '신발 및 신발 부분품 제조업'),
  ('161', '제재 및 목재 가공업'),
  ('162', '나무제품 제조업'),
  ('171', '펄프, 종이 및 판지 제조업'),
  ('172', '골판지, 종이 상자 및 종이 용기 제조업'),
  ('179', '기타 종이 및 판지 제품 제조업'),
  ('181', '인쇄 및 인쇄관련 산업'),
  ('182', '기록매체 복제업'),
  ('192', '석유 정제품 제조업'),
  ('201', '기초 화학물질 제조업'),
  ('202', '합성고무 및 플라스틱 물질 제조업'),
  ('203', '비료, 농약 및 살균ㆍ살충제 제조업'),
  ('204', '기타 화학제품 제조업'),
  ('205', '화학섬유 제조업'),
  ('211', '기초 의약 물질 및 생물학적 제제 제조업'),
  ('212', '의약품 제조업'),
  ('213', '의료용품 및 기타 의약 관련제품 제조업'),
  ('221', '고무제품 제조업'),
  ('222', '플라스틱 제품 제조업'),
  ('231', '유리 및 유리제품 제조업'),
  ('232', '내화, 비내화 요업제품 제조업'),
  ('233', '시멘트, 석회, 플라스터 및 그 제품 제조업'),
  ('239', '기타 비금속 광물제품 제조업'),
  ('241', '1차 철강 제조업'),
  ('242', '1차 비철금속 제조업'),
  ('243', '금속 주조업'),
  ('251', '구조용 금속제품, 탱크 및 증기발생기 제조업'),
  ('252', '무기 및 총포탄 제조업'),
  ('259', '기타 금속 가공제품 제조업'),
  ('261', '반도체 제조업'),
  ('262', '전자 부품 제조업'),
  ('263', '컴퓨터 및 주변 장치 제조업'),
  ('264', '통신 및 방송장비 제조업'),
  ('265', '영상 및 음향 기기 제조업'),
  ('266', '마그네틱 및 광학 매체 제조업'),
  ('271', '의료용 기기 제조업'),
  ('272', '측정, 시험, 항해, 제어 및 기타 정밀 기기 제조업; 광학 기기 제외'),
  ('273', '사진장비 및 광학 기기 제조업'),
  ('281', '전동기, 발전기 및 전기 변환ㆍ공급ㆍ제어 장치 제조업'),
  ('282', '일차전지 및 축전지 제조업'),
  ('283', '절연선 및 케이블 제조업'),
  ('284', '전구 및 조명장치 제조업'),
  ('285', '가정용 기기 제조업'),
  ('289', '기타 전기장비 제조업'),
  ('291', '일반 목적용 기계 제조업'),
  ('292', '특수 목적용 기계 제조업'),
  ('301', '자동차용 엔진 및 자동차 제조업'),
  ('302', '자동차 차체 및 트레일러 제조업'),
  ('303', '자동차 신품 부품 제조업'),
  ('304', '자동차 재제조 부품 제조업'),
  ('311', '선박 및 보트 건조업'),
  ('312', '철도장비 제조업'),
  ('313', '항공기, 우주선 및 부품 제조업'),
  ('319', '그 외 기타 운송장비 제조업'),
  ('320', '가구 제조업'),
  ('332', '악기 제조업'),
  ('333', '운동 및 경기용구 제조업'),
  ('339', '그 외 기타 제품 제조업'),
  ('351', '전기업'),
  ('352', '연료용 가스 제조 및 배관공급업'),
  ('353', '증기, 냉ㆍ온수 및 공기 조절 공급업'),
  ('382', '폐기물 처리업'),
  ('411', '건물 건설업'),
  ('412', '토목 건설업'),
  ('421', '기반조성 및 시설물 축조관련 전문공사업'),
  ('422', '건물설비 설치 공사업'),
  ('423', '전기 및 통신 공사업'),
  ('424', '실내건축 및 건축마무리 공사업'),
  ('451', '자동차 판매업'),
  ('452', '자동차 부품 및 내장품 판매업'),
  ('461', '상품 중개업'),
  ('462', '산업용 농ㆍ축산물 및 동ㆍ식물 도매업'),
  ('463', '음ㆍ식료품 및 담배 도매업'),
  ('464', '생활용품 도매업'),
  ('465', '기계장비 및 관련 물품 도매업'),
  ('467', '기타 전문 도매업'),
  ('468', '상품 종합 도매업'),
  ('471', '종합 소매업'),
  ('472', '음ㆍ식료품 및 담배 소매업'),
  ('473', '가전제품 및 정보 통신장비 소매업'),
  ('474', '섬유, 의복, 신발 및 가죽제품 소매업'),
  ('475', '기타 생활용품 소매업'),
  ('477', '연료 소매업'),
  ('478', '기타 상품 전문 소매업'),
  ('479', '무점포 소매업'),
  ('492', '육상 여객 운송업'),
  ('493', '도로 화물 운송업'),
  ('501', '해상 운송업'),
  ('511', '항공 여객 운송업'),
  ('529', '기타 운송관련 서비스업'),
  ('551', '일반 및 생활 숙박시설 운영업'),
  ('561', '음식점업'),
  ('581', '서적, 잡지 및 기타 인쇄물 출판업'),
  ('582', '소프트웨어 개발 및 공급업'),
  ('591', '영화, 비디오물, 방송 프로그램 제작 및 배급업'),
  ('592', '오디오물 출판 및 원판 녹음업'),
  ('602', '텔레비전 방송업'),
  ('612', '전기 통신업'),
  ('620', '컴퓨터 프로그래밍, 시스템 통합 및 관리업'),
  ('631', '자료 처리, 호스팅, 포털 및 기타 인터넷 정보 매개 서비스업'),
  ('639', '기타 정보 서비스업'),
  ('642', '신탁업 및 집합 투자업'),
  ('649', '기타 금융업'),
  ('661', '금융 지원 서비스업'),
  ('662', '보험 및 연금관련 서비스업'),
  ('681', '부동산 임대 및 공급업'),
  ('701', '자연과학 및 공학 연구개발업'),
  ('713', '광고업'),
  ('714', '시장 조사 및 여론 조사업'),
  ('715', '회사 본부 및 경영 컨설팅 서비스업'),
  ('716', '기타 전문 서비스업'),
  ('721', '건축 기술, 엔지니어링 및 관련 기술 서비스업'),
  ('729', '기타 과학기술 서비스업'),
  ('732', '전문 디자인업'),
  ('739', '그 외 기타 전문, 과학 및 기술 서비스업'),
  ('741', '사업시설 유지ㆍ관리 서비스업'),
  ('752', '여행사 및 기타 여행 보조 서비스업'),
  ('753', '경비, 경호 및 탐정업'),
  ('759', '기타 사업 지원 서비스업'),
  ('761', '운송장비 임대업'),
  ('762', '개인 및 가정용품 임대업'),
  ('763', '산업용 기계 및 장비 임대업'),
  ('851', '초등 교육기관'),
  ('855', '일반 교습학원'),
  ('856', '기타 교육기관'),
  ('857', '교육 지원 서비스업'),
  ('872', '비거주 복지시설 운영업'),
  ('901', '창작 및 예술관련 서비스업'),
  ('912', '유원지 및 기타 오락관련 서비스업'),
  ('953', '개인 및 가정용품 수리업'),
  ('969', '그 외 기타 개인 서비스업');

INSERT INTO industry_codes (industry_code, industry_name)
SELECT v.industry_code, v.industry_name
FROM (VALUES
  ('109', NULL),
  ('603', NULL)
) v(industry_code, industry_name)
WHERE NOT EXISTS (
  SELECT 1 FROM industry_codes ic WHERE ic.industry_code = v.industry_code
);
