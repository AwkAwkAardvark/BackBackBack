# QnA Admin Reply & Dynamic Status Specification

## 0. Agent Workflow Instructions
**Mandatory Protocol for Agents:**
1.  **Pre-Stage Check:** Before starting any stage (A, B, C, etc.), print the corresponding checklist section to the console to confirm scope.
2.  **Execution:** Execute the tasks strictly.
3.  **Post-Stage Verification:** After completing the tasks, print the checklist again. Verify that every item has been enacted faithfully in both letter and spirit.
4.  **Commit & Push:** Commit the changes (following convention) and push to `origin`.
5.  **Halt:** **STOP** and wait for explicit user confirmation before proceeding to the next stage.

---

## 1. Objective
Implement an administrative reply system specifically for the Q&A board. This includes a new API endpoint for admins to respond to questions and a computed "qnaStatus" field in the post response to reflect whether a question is "pending" or "answered" based on the presence of an admin reply.

---

## 2. Impact Assessment

| Component | File Path | Impact |
| :--- | :--- | :--- |
| **DTO** | `PostResponse.java` | **Medium**: Added `qnaStatus` field. |
| **DTO** | `QaReplyInput.java` | **New**: Request body for admin replies. |
| **DTO** | `QaReplyResponse.java` | **New**: Response body for admin replies. |
| **Mapper** | `PostMapper.java` | **High**: Custom logic to compute `qnaStatus` from replies list. |
| **Controller** | `AdminPostController.java` | **Medium**: Added `/qna/{postId}/replies` endpoint. |
| **Service** | `CommentsService.java` | **Medium**: Logic to handle admin-specific replies. |

---

## 3. Implementation Checklist

### A. DTO Development
- [ ] **QaReplyInput.java**: Define with `content` field.
- [ ] **QaReplyResponse.java**: Define based on `CommentResponse` structure.
- [ ] **PostResponse.java**: Add `@Schema(description = "QnA 상태", example = "pending") String qnaStatus`.

### B. Domain & Logic (Computed Status)
- [ ] **PostMapper.java**: 
    - [ ] Update to map `qnaStatus`.
    - [ ] Logic: If category is "qna", check `replies`. If at least one reply is from a user with `ROLE_ADMIN`, status is `"answered"`, else `"pending"`.
    - [ ] For other categories, `qnaStatus` should be `null` or omitted.

### C. Service & API Layer
- [ ] **CommentsService.java**:
    - [ ] Add `createAdminReply` method.
    - [ ] Ensure it links the comment to the post and the admin user.
- [ ] **AdminPostController.java**:
    - [ ] Add `POST /api/admin/posts/qna/{postId}/replies`.
    - [ ] Restrict usage to the "qna" category.

### D. Verification & Tests
- [ ] **PostMapperTest.java**: Verify status computation logic.
- [ ] **AdminPostControllerIntegrationTest.java**:
    - [ ] Test successful admin reply creation.
    - [ ] Verify `qnaStatus` transitions from "pending" to "answered" in subsequent GET requests.
- [ ] **PostControllerIntegrationTest.java**: Ensure users see the correct `qnaStatus`.

---

## 4. Considerations & Caveats

### Definition of "Answered"
- A post is considered **answered** if any comment exists that was authored by a user with the `ROLE_ADMIN` role. 
- Deleting an admin reply will automatically revert the status to **pending** since the status is computed dynamically.

### Performance
- Computing status by traversing the `replies` list in the Mapper is efficient for standard QnA threads but should be monitored if threads become exceptionally long.
