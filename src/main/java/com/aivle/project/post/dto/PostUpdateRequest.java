package com.aivle.project.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시글 수정 요청 DTO.
 */
@Schema(description = "게시글 수정 요청")
@Getter
@Setter
public class PostUpdateRequest {

	@Positive
	@Schema(description = "카테고리 ID", example = "3")
	private Long categoryId;

	@Size(max = 200)
	@Schema(description = "제목", example = "수정된 제목")
	private String title;

	@Schema(description = "내용", example = "수정된 내용입니다.")
	private String content;
}
