# DART 기업 목록 동기화 요구사항

## 1. 목적
- DART corpCode ZIP/XML을 이용해 기업 목록을 초기 적재하고, 이후 변경분만 갱신한다.

## 2. 확정 요구사항
- 초기 적재 후 **변경분 갱신** 전략을 사용한다.
- 스케줄러는 **기본 OFF**, 필요 시 활성화한다.
- **관리자용 수동 실행 API**를 제공한다. (스케줄링과 독립)
- API Key 환경변수 이름은 **DART_API_KEY**로 통일한다.

## 3. 동기화 범위
- 대상 엔드포인트: `/api/corpCode.xml` (ZIP 내 XML)
- 매핑 대상 컬럼: `corp_code`, `corp_name`, `corp_eng_name`, `stock_code`, `modify_date`
- 식별자 기준: `corp_code` 유니크

## 4. 후속 작업(요약)
- Batch Job/Step 설계 및 스캐폴딩
- 변경분 갱신(업서트) 로직 구현
- 수동 실행 API + 스케줄 토글 구성
- 테스트/운영 가이드 정리

---

## 5. 운영 가이드

### 5.1 환경 변수
- `DART_API_KEY`: DART API 인증키 (필수)

### 5.2 수동 실행 API
- `POST /admin/dart/corp-sync`
- 권한: `ROLE_ADMIN`
- 응답: 202 Accepted (jobExecutionId, status)

### 5.3 스케줄러 설정
- 기본 OFF: `dart.corp-sync.schedule.enabled=false`
- 크론: `dart.corp-sync.schedule.cron` (기본 `0 0 3 * * *`)

### 5.4 네트워크/재시도 설정
- `dart.http.connect-timeout-ms` (기본 3000)
- `dart.http.response-timeout-ms` (기본 10000)
- `dart.http.retry-count` (기본 2)
- `dart.http.retry-backoff-ms` (기본 1000)
- `dart.api.max-buffer-size` (기본 50MB)

### 5.5 배치 이력 테이블
- Flyway `V6__batch_schema.sql`로 배치 메타 테이블을 생성한다.
