package com.aivle.project.auth.exception;

import com.aivle.project.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 인증/인가 에러 코드.
 */
public enum AuthErrorCode implements ErrorCode {
	INVALID_CREDENTIALS("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
	EMAIL_VERIFICATION_REQUIRED("EMAIL_VERIFICATION_REQUIRED", "이메일 인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
	EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
	INVALID_REFRESH_TOKEN("AUTH_401", "인증에 실패했습니다. (토큰 만료 또는 유효하지 않음)", HttpStatus.UNAUTHORIZED),
	PASSWORD_EXPIRED("PASSWORD_EXPIRED", "비밀번호가 만료되었습니다. 비밀번호를 변경해주세요.", HttpStatus.FORBIDDEN);

	private final String code;
	private final String message;
	private final HttpStatus status;

	AuthErrorCode(String code, String message, HttpStatus status) {
		this.code = code;
		this.message = message;
		this.status = status;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
