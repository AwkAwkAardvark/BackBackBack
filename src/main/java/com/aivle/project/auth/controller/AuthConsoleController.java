package com.aivle.project.auth.controller;

import com.aivle.project.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 로그인/로그아웃 검증용 콘솔 페이지 (dev 전용).
 */
@Profile("dev")
@Controller
@RequestMapping("/auth/console")
@Tag(name = "개발", description = "개발용 인증 콘솔 API")
public class AuthConsoleController {

	@GetMapping
	@Operation(hidden = true)
	public String console() {
		return "auth-console";
	}

	@GetMapping("/claims")
	@ResponseBody
	@Operation(summary = "토큰 클레임 조회", description = "현재 토큰의 클레임을 확인합니다.")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
	})
	public ApiResponse<Map<String, Object>> claims(@AuthenticationPrincipal Jwt jwt) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("sub", jwt.getSubject());
		response.put("email", jwt.getClaimAsString("email"));
		response.put("roles", jwt.getClaimAsStringList("roles"));
		response.put("deviceId", jwt.getClaimAsString("deviceId"));
		response.put("issuer", jwt.getClaimAsString("iss"));
		response.put("jti", jwt.getId());
		response.put("issuedAt", formatInstant(jwt.getIssuedAt()));
		response.put("expiresAt", formatInstant(jwt.getExpiresAt()));
		return ApiResponse.ok(response);
	}

	private String formatInstant(Instant instant) {
		if (instant == null) {
			return null;
		}
		return instant.toString();
	}
}
