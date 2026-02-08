package com.aivle.project.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * QnA 관리자 답변 응답 DTO.
 */
@Schema(description = "QnA 관리자 답변 응답")
public record QaReplyResponse(
	@Schema(description = "답변 ID", example = "10")
	Long id,
	@Schema(description = "작성자 성명", example = "관리자")
	String name,
	@Schema(description = "게시글 ID", example = "100")
	Long postId,
	@Schema(description = "답변 내용", example = "문의하신 내용에 대한 답변입니다.")
	String content,
	@Schema(description = "생성 일시", example = "2026-02-08T12:34:56")
	LocalDateTime createdAt,
	@Schema(description = "수정 일시", example = "2026-02-08T12:40:00")
	LocalDateTime updatedAt
) {
}
