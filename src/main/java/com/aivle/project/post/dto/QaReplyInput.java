package com.aivle.project.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 관리자 답변 작성 요청")
public class QaReplyInput {

	@NotBlank(message = "내용은 필수입니다.")
	@Schema(description = "답변 내용", example = "문의하신 내용에 대한 답변입니다.")
	private String content;
}
