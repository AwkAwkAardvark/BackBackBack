# 프론트엔드-백엔드 연동 가이드

## 1. 개요

이 문서는 로컬 개발 환경에서 프론트엔드 애플리케이션을 백엔드 API 서버와 연결하기 위해 필요한 설정 변경 사항을 안내합니다. 현재 프론트엔드 코드는 Mock API를 사용하도록 설정되어 있어, 실제 백엔드와 통신하려면 아래의 두 가지 주요 변경이 필요합니다.

---

## 2. 변경 사항 안내

### 1단계: Vite 개발 서버 프록시 설정

로컬 프론트엔드 개발 서버(port 3000)에서 발생하는 API 요청(경로가 `/api`로 시작하는 모든 요청)을 로컬 백엔드 서버(port 8080)로 전달해야 합니다. 이를 위해 `vite.config.ts` 파일에 프록시 설정을 추가합니다.

**경로:** `.local/front-main/vite.config.ts`

**변경 내용:**
```typescript
import path from 'path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 백엔드 Spring Boot 서버가 8080 포트에서 실행된다고 가정합니다.
const BACKEND_PORT = 8080;

export default defineConfig(() => ({
  base: '/',
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      // '/api'로 시작하는 모든 요청을 백엔드 서버로 전달합니다.
      '/api': {
        target: `http://localhost:${BACKEND_PORT}`,
        changeOrigin: true,
      },
    },
  },
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, '.'),
    }
  }
}));
```

### 2단계: Mock API 비활성화

코드 전역에 설정된 Mock API 사용 플래그를 `false`로 변경하여 실제 API를 호출하도록 해야 합니다. 두 개의 파일을 수정해야 합니다.

1.  **데이터 API Mock 비활성화**
    *   **파일:** `.local/front-main/src/services/http.ts`
    *   **변경:** `const USE_MOCK_API = true;` 값을 `false`로 수정합니다.

2.  **인증 API Mock 비활성화**
    *   **파일:** `.local/front-main/src/services/auth.ts`
    *   **변경:** `const USE_MOCK_AUTH = true;` 값을 `false`로 수정합니다.

---

위의 모든 변경 사항(Vite 프록시, Mock API 비활성화, 회원가입 엔드포인트 수정)을 적용하면 프론트엔드 애플리케이션이 실제 백엔드 API와 정상적으로 통신하게 됩니다.
