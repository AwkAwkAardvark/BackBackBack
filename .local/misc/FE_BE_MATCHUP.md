# FE/BE Matchup & Connection Report

## 1. Executive Summary

The frontend application is **fully disconnected** from the backend. Communication is not possible in its current state. This is due to two primary reasons:

1.  **Global Mocking is Enabled:** All API calls, including authentication, are intercepted and routed to mock data generators.
2.  **Missing Network Proxy:** The frontend development server is not configured to proxy API requests to the backend server, which would cause all live API calls to fail even if mocking were disabled.

To establish a connection, mocking must be disabled, and a network proxy must be configured.

---

## 2. How to Connect FE to BE

Follow these two steps to enable live communication.

### Step 1: Add Server Proxy to Vite Config

The frontend development server (running on port 3000) needs to know where the backend API server is (e.g., `http://localhost:8080`). Add a `server.proxy` configuration to `.local/front-main/vite.config.ts`.

**Replace the content of `vite.config.ts` with this:**

```typescript
import path from 'path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Assuming the Spring Boot backend runs on port 8080
const BACKEND_PORT = 8080;

export default defineConfig(() => ({
  base: '/',
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      // Proxy all requests starting with /api to the backend server
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

### Step 2: Disable Mock API Flags

You must set the two mock flags to `false`.

1.  **In `.local/front-main/src/services/http.ts`:**
    *   Change `const USE_MOCK_API = true;`
    *   To `const USE_MOCK_API = false;`

2.  **In `.local/front-main/src/services/auth.ts`:**
    *   Change `const USE_MOCK_AUTH = true;`
    *   To `const USE_MOCK_AUTH = false;`

---

## 3. Endpoint Matchup Analysis

The following table details the status of each API endpoint based on the backend's OpenAPI specification and the frontend's implementation.

| Feature | BE Endpoint | FE Implementation | Status | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **User Login** | POST /api/auth/login | login() in services/auth.ts | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_AUTH`. |
| **Get Current User** | `GET /auth/me` | *Not Implemented* | **Missing FE Code** | The FE currently stores the user object from the login response in local storage. It does not call this endpoint. |
| **User Logout** | POST /api/auth/logout | logout() in services/auth.ts | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_AUTH`. |
| **Dashboard Summary** | `GET /dashboard/summary` | `getDashboardSummary()` in `api/companies.ts` | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_API`. |
| **Search Companies** | `GET /companies/search` | `searchCompanies()` in `api/companies.ts` | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_API`. |
| **Confirm Company**| `POST /companies/confirm` | `confirmCompany()` in `api/companies.ts` | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_API`. |
| **Company Overview**| `GET /companies/{id}/overview` | `getCompanyOverview()` in `api/companies.ts` | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_API`. |
| **Create Update Request** | `POST /companies/{id}/update-requests` | `createUpdateRequest()` in `api/companies.ts` | **Blocked by Mock** | The FE code exists but is disabled by `USE_MOCK_API`. |
| **Decision Room API** | *(Various)* | `services/decisionRoomApi.ts` | **Not Implemented** | All API calls in this service are commented out and rely on mock data. The BE has no corresponding endpoints defined in the OpenAPI spec. |

---

## 4. Authentication Flow Notes

*   **Access Token:** The FE correctly sends the access token as a `Bearer` token in the `Authorization` header for requests made via `http.ts`.
*   **Refresh Token:** The BE sends the refresh token as a secure `HttpOnly` cookie. The frontend currently has **no logic** to handle this. For login persistence via refresh tokens to work, API requests (especially to a refresh endpoint) must be configured to include credentials (cookies).
| **User Registration** | POST /api/auth/signup | signup() in services/auth.ts | **Fixed** | The FE now correctly calls the /api/auth/signup endpoint. |
