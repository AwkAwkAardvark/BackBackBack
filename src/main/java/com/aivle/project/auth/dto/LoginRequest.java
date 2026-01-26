package com.aivle.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 DTO.
 */
@Schema(description = "로그인 요청")
@Getter
@Setter
public class LoginRequest {

	@NotBlank
	@Email
	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@NotBlank
	@Schema(description = "비밀번호", example = "P@ssw0rd!")
	private String password;

	@Schema(description = "디바이스 ID", example = "device-001")
	private String deviceId;

	@Schema(description = "디바이스 정보", example = "iPhone 15 Pro / iOS 17")
	private String deviceInfo;
}
