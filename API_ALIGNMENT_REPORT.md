# API 연동 현황 리포트: Auth API (Spec v1.2.0 기준)

이 리포트는 업데이트된 Frontend 명세서(`SENTINEL_PoC_API.yaml v1.2.0`)와 현재 Backend 구현(`feat/align-auth-api` 브랜치) 간의 일치 여부를 비교합니다.

## **1. 비교 요약 (Comparison Chart)**

| **기능 (Feature)** | **FE 명세 (v1.2.0)** | **BE 구현 (Current)** | **상태 (Status)** |
| :--- | :--- | :--- | :--- |
| **로그인 엔드포인트** | `POST /auth/login` | `POST /auth/login` | ✅ **일치** |
| **로그인 요청 본문** | `{ "email": "...", "password": "..." }` | `{ "email": "...", "password": "...", ... }` | ✅ **일치** (Spec이 email로 변경됨) |
| **로그인 응답 포맷** | Direct Object (`LoginResponse`) | Raw Object (`TokenResponse`) | ✅ **일치** (Wrapper 불필요) |
| **로그인 응답 데이터** | `accessToken`, `expiresIn`, `tokenType`, **`user`** | `accessToken`, `expiresIn`, `tokenType` | ❌ **불일치** (`user` 객체 누락) |
| **사용자 정보 객체** | `UserSummary` (`userId`, `email`, `name`, `role`) | **없음** | ❌ **불일치** |
| **Refresh Token 전달** | (언급 없음 / Spec에서 제거됨) | **HttpOnly Cookie** (`refresh_token`) | ✅ **일치** (보안) |
| **로그아웃** | `POST /auth/logout` (204 No Content) | `POST /auth/logout` (204 No Content) | ✅ **일치** |

## **2. 주요 변경 사항 (Delta Analysis)**

### **A. 요청 필드 (Request Field)**
*   **이전 (v0.1.0)**: `username` 사용으로 인한 불일치 발생.
*   **현재 (v1.2.0)**: Spec이 `email`로 변경되어 Backend 구현과 **자동 일치**됨. 별도 수정 불필요.

### **B. 응답 구조 (Response Structure)**
*   **이전 (v0.1.0)**: `ApiResponse` 래퍼 요구.
*   **현재 (v1.2.0)**: 래퍼 없이 직접 객체 반환 (`LoginResponse`). Backend 구현과 **일치**함. `ApiResponse` 적용 작업 취소.

### **C. 사용자 정보 (User Summary)**
*   **요구 사항**: 로그인 응답에 `user` 객체(`UserSummary`)가 포함되어야 함.
    ```json
    "user": {
      "userId": "...",
      "email": "...",
      "name": "...",
      "role": "..."
    }
    ```
*   **조치 필요**: `TokenResponse`에 `UserSummaryDto` 필드를 추가하고 `AuthService`에서 채워주어야 함.

## **3. 다음 작업 (Next Steps)**

1.  [ ] **[BE]** `UserSummaryDto` 클래스 생성 (`UserRefDto` 대체)
2.  [ ] **[BE]** `TokenResponse`에 `UserSummaryDto` 필드 추가
3.  [ ] **[BE]** `AuthService` 로그인 로직 수정 (User 정보 매핑)
