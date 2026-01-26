package com.aivle.project.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시글 생성 요청 DTO.
 */
@Schema(description = "게시글 생성 요청")
@Getter
@Setter
public class PostCreateRequest {

	@NotNull
	@Schema(description = "카테고리 ID", example = "3")
	private Long categoryId;

	@NotBlank
	@Size(max = 200)
	@Schema(description = "제목", example = "첫 번째 게시글")
	private String title;

	@NotBlank
	@Schema(description = "내용", example = "게시글 내용입니다.")
	private String content;
}
