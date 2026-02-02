package com.aivle.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 이메일 인증 결과 응답 DTO.
 */
@Schema(description = "이메일 인증 결과")
public record EmailVerificationResultResponse(
	@Schema(description = "인증 상태", example = "success")
	String status,
	@Schema(description = "상세 메시지", example = "이메일 인증이 완료되었습니다.")
	String message
) {
}
