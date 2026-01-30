package com.aivle.project.file.dto;

import com.aivle.project.file.entity.FilesEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 파일 업로드 응답 DTO.
 */
@Schema(description = "파일 응답")
public record FileResponse(
	@Schema(description = "파일 ID", example = "1")
	Long id,
	@Schema(description = "게시글 ID", example = "100")
	Long postId,
	@Schema(description = "저장 경로 또는 URL", example = "https://cdn.example.com/files/1")
	String storageUrl,
	@Schema(description = "원본 파일명", example = "document.pdf")
	String originalFilename,
	@Schema(description = "파일 크기(바이트)", example = "102400")
	long fileSize,
	@Schema(description = "콘텐츠 타입", example = "application/pdf")
	String contentType,
	@Schema(description = "업로드 일시", example = "2026-01-25T12:34:56")
	LocalDateTime createdAt
) {
}
