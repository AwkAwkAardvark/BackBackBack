# API Alignment Report: Auth API

This report compares the Frontend Specification (`SENTINEL_PoC_API.yaml`) with the current Backend Implementation (`feat/align-auth-api` branch).

## **1. Comparison Chart**

| **Feature** | **Frontend Spec (SENTINEL_PoC_API.yaml)** | **Backend Implementation (Current)** | **Alignment Status** |
| :--- | :--- | :--- | :--- |
| **Login Endpoint** | `POST /auth/login` | `POST /auth/login` | ✅ **Aligned** |
| **Login Request Body** | `{ "username": "...", "password": "..." }` | `{ "email": "...", "password": "...", "deviceId": "...", ... }` | ⚠️ **Mismatch** (`username` vs `email`) |
| **Login Response Format** | Wrapped Object: `ApiResponseAuthLogin` <br> (`success`, `data`, `error`, `timestamp`) | Raw Object: `TokenResponse` | ❌ **Mismatch** (Missing Wrapper) |
| **Login Response Data** | Fields: `accessToken`, `refreshToken`, `expiresIn`, **`user`** | Fields: `accessToken`, `expiresIn`, `passwordExpired` <br> (Hidden: `refreshToken`) | ❌ **Mismatch** (Missing `user` object) |
| **User Info Object** | `{ "id": 1, "name": "...", "role": "USER" }` | **Missing** | ❌ **Mismatch** |
| **Refresh Token Delivery** | JSON Body | **HttpOnly Cookie** (`refresh_token`) | ✅ **Intentional Deviation** (Security Best Practice) |
| **Logout Endpoint** | `POST /auth/logout` | `POST /auth/logout` | ✅ **Aligned** (Implemented) |

## **2. Detailed Discrepancies**

### **A. Response Wrapper**
*   **Spec**: Expects a standard envelope `ApiResponse<T>` for all successful responses.
*   **Current**: Returns the DTO directly.
*   **Action Required**: Modify `AuthController` to return `ApiResponse<TokenResponse>`.

### **B. User Identity in Response**
*   **Spec**: Login response must include the logged-in user's `id`, `name`, and `role`.
*   **Current**: Only token information is returned.
*   **Action Required**:
    1.  Create `UserRefDto`.
    2.  Add `UserRefDto` field to `TokenResponse`.
    3.  Populate this data in `AuthService`.

### **C. Request Parameter Name**
*   **Spec**: `username`
*   **Current**: `email`
*   **Note**: Existing frontend code (`auth-console.html`) uses `email`.
*   **Recommendation**: Stick to `email` in Backend. The YAML spec should ideally be updated, or we can alias `username` to `email` if strict compliance is required.

## **3. Next Steps**

1.  [ ] Create `UserRefDto` class.
2.  [ ] Refactor `TokenResponse` to include `UserRefDto`.
3.  [ ] Refactor `AuthService.login` to populate user details.
4.  [ ] Refactor `AuthController` to wrap responses in `ApiResponse`.
