package com.aivle.project.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 필드 단위 검증 에러 응답.
 */
@Schema(description = "필드 검증 오류")
public record FieldErrorResponse(
	@Schema(description = "필드명", example = "email")
	String field,
	@Schema(description = "오류 메시지", example = "이메일 형식이 올바르지 않습니다.")
	String message
) {
}
