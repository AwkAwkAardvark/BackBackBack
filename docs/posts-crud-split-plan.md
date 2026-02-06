# Posts CRUD Split Plan (Board-based System)

## 1. Current Status (Path-based Refactoring)

Currently, the codebase uses a flat `/api/posts` structure where the category is passed as a field in the DTO. We are moving to a **Board-based Pattern** where the category name is part of the URI path.

| Endpoint (Board Pattern) | Current DTO | Pending DTO | Status |
| :--- | :--- | :--- | :--- |
| **[User]** `GET /api/posts/{categoryName}` | `PageRequest` (params) | `PageRequest` | **Does not exist** |
| **[User]** `GET /api/posts/{categoryName}/{postId}` | N/A | `PostResponse` | **Does not exist** |
| **[User]** `POST /api/posts/{categoryName}` | `PostCreateRequest` | `PostUserCreateRequest` (Remove `categoryId`) | **Does not exist** |
| **[User]** `PATCH /api/posts/{categoryName}/{postId}` | `PostUpdateRequest` | `PostUserUpdateRequest` (Remove `categoryId`) | **Does not exist** |
| **[User]** `DELETE /api/posts/{categoryName}/{postId}` | N/A | N/A | **Does not exist** |
| **[Admin]** `GET /api/admin/posts/{categoryName}` | N/A | `PageRequest` | **Does not exist** |
| **[Admin]** `GET /api/admin/posts/{categoryName}/{postId}` | N/A | `PostResponse` | **Does not exist** |
| **[Admin]** `POST /api/admin/posts/{categoryName}` | N/A | `PostAdminCreateRequest` (+`isPinned`, `status`) | **Does not exist** |
| **[Admin]** `PATCH /api/admin/posts/{categoryName}/{postId}` | N/A | `PostAdminUpdateRequest` (+`isPinned`, `status`) | **Does not exist** |
| **[Admin]** `DELETE /api/admin/posts/{categoryName}/{postId}` | N/A | N/A | **Does not exist** |

---

## 2. Implementation Checklist

### A. Infrastructure & Data
- [x] Create Flyway migration `V16__seed_categories.sql` to seed `notices` (ID: 1) and `qna` (ID: 2).
- [x] Remove redundant `DevCategorySeeder.java` to prevent naming/logic conflicts in `dev` profile.
- [ ] Add `industry_code` column to `companies` table if not already present (verify V13 status).

### B. DTO Refactoring
- [x] Create `PostUserCreateRequest` / `PostUserUpdateRequest` (Remove `categoryId`).
- [x] Create `PostAdminCreateRequest` / `PostAdminUpdateRequest` (Add `isPinned`, `status`).

### C. Domain & Service Layer
- [x] **PostsRepository:** Add methods to find by `categoryName` + `PostStatus` + `DeletedAtIsNull`.
- [x] **PostsEntity:** Update `update` method to support `isPinned` and `status` for admin use.
- [x] **PostService:**
    - [x] Refactor to use `categoryName` lookup.
    - [x] Implement `Admin` service methods (bypass ownership checks, full visibility).
    - [x] Enforce Board Logic:
        - `notices`: User = Read-only (GET); Admin = Full CRUD.
        - `qna`: User = Full CRUD (Own posts only, including GET); Admin = Full CRUD (All posts).

### D. API Layer (Controllers)
- [x] **PostController (`/api/posts/{categoryName}`)**: Implement user-facing board logic.
- [x] **AdminPostController (`/api/admin/posts/{categoryName}`)**: Implement admin-facing management logic.
- [x] **SecurityConfig**: Update request matchers to protect `/api/admin/posts/**` with `hasRole('ADMIN')`.

### E. Cleanup & Deprecation
- [x] Delete existing `com.aivle.project.qna` package (Controller, Service, DTOs, Mapper).
- [x] Remove any specific references to `/api/qna` in frontend-facing documentation or tests.

### F. Generate Tests
- [ ] Create unit tests for `PostService` (Board logic, Admin vs User).
- [ ] Create integration tests for `PostController` and `AdminPostController`.
- [ ] Verify security rules in `SecurityConfig` via WebMvcTests.

---

## 3. Overview & Context Instructions

### Core Philosophy
Each category in the database is treated as a separate "Board". The board logic is determined by the `categoryName` in the URL. This generalized system **supersedes and replaces** the previous specific QnA module (`com.aivle.project.qna`). 

Security is handled via a mix of Spring Security (for the `/admin` prefix) and Service-layer logic (for ownership and board-specific rules).

### Overwrite Strategy
The existing `QnaService` and `QnaController` are technical debt that will be broken and eventually removed once the generalized `/api/posts/{categoryName}` logic is functional. Frontend clients should migrate from board-specific endpoints to the generic board pattern.

### Board Rules
1. **Notices (`/notices`)**:
    - **Users**: Can only view the list and details (`GET`). Attempting to Write/Edit/Delete returns `403 Forbidden`.
    - **Admins**: Full control. Can pin posts and set status.
2. **QnA (`/qna`)**:
    - **Users**: Can create posts. Can only view, update, or delete posts **they created**.
    - **Admins**: Full control over all posts in the QnA board.

### Development Direction
- Strictly separate User and Admin controllers to keep the code clean and prevent accidental exposure of admin fields (`isPinned`, `status`) to user endpoints.
- Ownership must be verified in the Service layer using the `CurrentUser` provided by the controller.
- Use `categoryName` (e.g., "notices", "qna") as the primary identifier in the URL path.
