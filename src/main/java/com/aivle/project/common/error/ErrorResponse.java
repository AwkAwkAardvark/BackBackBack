package com.aivle.project.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 공통 에러 응답 포맷.
 */
@Schema(description = "에러 응답")
public record ErrorResponse(
	@Schema(description = "에러 코드", example = "AUTH_001")
	String code,
	@Schema(description = "에러 메시지", example = "인증에 실패했습니다.")
	String message,
	@Schema(description = "응답 생성 시각(UTC, ISO-8601)", example = "2026-01-25T12:34:56Z")
	String timestamp,
	@Schema(description = "요청 경로", example = "/auth/login")
	String path,
	@Schema(description = "필드 검증 오류 목록")
	List<FieldErrorResponse> errors
) {

	public static ErrorResponse of(String code, String message, String path) {
		String timestamp = OffsetDateTime.now(ZoneOffset.UTC).toString();
		return new ErrorResponse(code, message, timestamp, path, null);
	}

	public static ErrorResponse of(ErrorCode code, String path, String timestamp) {
		return new ErrorResponse(code.getCode(), code.getMessage(), timestamp, path, null);
	}

	public static ErrorResponse of(ErrorCode code, String path, String timestamp, List<FieldErrorResponse> errors) {
		return new ErrorResponse(code.getCode(), code.getMessage(), timestamp, path, errors);
	}
}
