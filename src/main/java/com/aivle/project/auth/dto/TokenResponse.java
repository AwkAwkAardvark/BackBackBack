package com.aivle.project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 응답 DTO.
 */
@Schema(description = "토큰 응답")
public record TokenResponse(
	@Schema(description = "토큰 타입", example = "Bearer")
	String tokenType,
	@Schema(description = "액세스 토큰", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
	String accessToken,
	@Schema(description = "액세스 토큰 만료(초)", example = "1800")
	long expiresIn,
	@Schema(description = "리프레시 토큰", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
	String refreshToken,
	@Schema(description = "리프레시 토큰 만료(초)", example = "604800")
	long refreshExpiresIn
) {
	public static TokenResponse of(String accessToken, long accessExpiresIn, String refreshToken, long refreshExpiresIn) {
		return new TokenResponse("Bearer", accessToken, accessExpiresIn, refreshToken, refreshExpiresIn);
	}
}
