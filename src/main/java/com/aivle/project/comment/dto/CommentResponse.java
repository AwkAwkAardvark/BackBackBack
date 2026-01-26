package com.aivle.project.comment.dto;

import com.aivle.project.comment.entity.CommentsEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO.
 */
@Schema(description = "댓글 응답")
public record CommentResponse(
	@Schema(description = "댓글 ID", example = "10")
	Long id,
	@Schema(description = "작성자 ID", example = "1")
	Long userId,
	@Schema(description = "게시글 ID", example = "100")
	Long postId,
	@Schema(description = "부모 댓글 ID", example = "5")
	Long parentId,
	@Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
	String content,
	@Schema(description = "댓글 깊이", example = "0")
	int depth,
	@Schema(description = "댓글 정렬 순서", example = "1")
	int sequence,
	@Schema(description = "생성 일시", example = "2026-01-25T12:34:56")
	LocalDateTime createdAt,
	@Schema(description = "수정 일시", example = "2026-01-25T12:40:00")
	LocalDateTime updatedAt
) {
	public static CommentResponse from(CommentsEntity comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getUser().getId(),
			comment.getPost().getId(),
			comment.getParent() != null ? comment.getParent().getId() : null,
			comment.getContent(),
			comment.getDepth(),
			comment.getSequence(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}
}
