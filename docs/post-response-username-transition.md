# Post Response User Name Refactoring Plan

## 1. Objective
Replace the `userId` field (Long) with `name` (String) in the `PostResponse` DTO to provide human-readable author information directly in the API response. This change improves frontend display efficiency by reducing the need for additional user metadata lookups.

---

## 2. Impact Assessment

| Component | File Path | Impact |
| :--- | :--- | :--- |
| **DTO** | `PostResponse.java` | **High**: Breaking change in API response structure. |
| **Mapper** | `PostMapper.java` | **Medium**: Update MapStruct mapping source from `user.id` to `user.name`. |
| **Service Tests** | `PostServiceTest.java` | **High**: Multiple constructor calls and assertions need updating. |
| **Integration Tests** | `PostControllerIntegrationTest.java` | **Medium**: Assertions on `userId` will fail; need to verify `name`. |
| **Consistency** | `CommentResponse.java` | **Low**: Should be updated for consistency across the board. |

---

## 3. Implementation Checklist

### A. DTO Refactoring
- [x] **PostResponse.java**:
    - [x] Rename `userId` to `name`.
    - [x] Change type from `Long` to `String`.
    - [x] Update `@Schema` description and example.
- [x] (Optional/Consistency) **CommentResponse.java**:
    - [x] Apply similar changes to replace `userId` with `name`.

### B. Mapping Logic
- [x] **PostMapper.java**:
    - [x] Update `@Mapping` source for `name` to `user.name`.
- [x] (If consistency applied) **CommentMapper.java**:
    - [x] Update mapping for comments.

### C. Test Refactoring
- [x] **PostServiceTest.java**:
    - [x] Update all `new PostResponse(...)` mock instantiations.
    - [x] Update assertions to check for name strings (e.g., "test-user") instead of IDs.
- [x] **PostControllerIntegrationTest.java**:
    - [x] Remove or update `assertThat(apiResponse.data().userId())`.
    - [x] Add `assertThat(apiResponse.data().name()).isEqualTo("test-user")`.

### D. Verification
- [ ] Verify build via `./gradlew classes`.
- [ ] Run post-domain specific tests: `./gradlew test --tests "com.aivle.project.post.*"`.
- [ ] Run full test suite to ensure no regressions.

---

## 4. Considerations & Caveats

### API Compatibility
- This is a **breaking change** for the API. Any client currently using `userId` for logic (e.g., "Delete" button visibility based on ID comparison) will need to be updated to use another unique identifier if available, or the change should be adjusted to **add** `name` instead of replacing `userId`.
- *Recommendation*: If frontend logic depends on IDs, consider having both `userId` and `name`. However, per current requirement, we proceed with replacement.

### Uniqueness
- `name` (from `UserEntity.name`) is used for display. If the system allows duplicate display names, this field should not be used for unique identification.
