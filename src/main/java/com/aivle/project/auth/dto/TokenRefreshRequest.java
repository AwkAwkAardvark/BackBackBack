package com.aivle.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 토큰 재발급 요청 DTO.
 */
@Schema(description = "토큰 재발급 요청")
@Getter
@Setter
public class TokenRefreshRequest {

	@NotBlank
	@Schema(description = "리프레시 토큰", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
	private String refreshToken;
}
