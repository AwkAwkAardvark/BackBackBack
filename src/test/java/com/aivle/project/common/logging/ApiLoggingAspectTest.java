package com.aivle.project.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.CookieValue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiLoggingAspectTest {

	private ApiLoggingAspect apiLoggingAspect;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		apiLoggingAspect = new ApiLoggingAspect(objectMapper);
	}

	@Test
	@DisplayName("비밀번호 필드는 마스킹되어야 한다")
	void mask_shouldMaskPassword() {
		Map<String, String> map = new HashMap<>();
		map.put("email", "test@example.com");
		map.put("password", "secret123");

		String result = apiLoggingAspect.mask(map);

		// 이메일도 민감한 정보이므로 마스킹됨
		assertThat(result).contains("\"email\":\"te***@example.com\"");
		assertThat(result).contains("\"password\":\"****\"");
		assertThat(result).doesNotContain("secret123");
	}

	@Test
	@DisplayName("토큰 필드는 마스킹되어야 한다")
	void mask_shouldMaskTokens() {
		Map<String, String> map = new HashMap<>();
		map.put("accessToken", "abc.def.ghi");
		map.put("refreshToken", "xyz.uvw.rst");

		String result = apiLoggingAspect.mask(map);

		assertThat(result).contains("\"accessToken\":\"****\"");
		assertThat(result).contains("\"refreshToken\":\"****\"");
		assertThat(result).doesNotContain("abc.def.ghi");
		assertThat(result).doesNotContain("xyz.uvw.rst");
	}

	@Test
	@DisplayName("단순 문자열이나 숫자는 마스킹되지 않아야 한다")
	void mask_shouldNotMaskSimpleTypes() {
		assertThat(apiLoggingAspect.mask("hello")).isEqualTo("\"hello\"");
		assertThat(apiLoggingAspect.mask(123)).isEqualTo("123");
	}

	@Test
	@DisplayName("Bearer 토큰 문자열은 마스킹되어야 한다")
	void mask_shouldMaskBearerTokenString() {
		String result = apiLoggingAspect.mask("Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature");
		assertThat(result).isEqualTo("\"Bearer ****\"");
	}

	@Test
	@DisplayName("JWT 토큰 문자열은 마스킹되어야 한다")
	void mask_shouldMaskJwtTokenString() {
		String result = apiLoggingAspect.mask("eyJhbGciOiJIUzI1NiJ9.payload.signature");
		assertThat(result).isEqualTo("\"****\"");
	}

	@Test
	@DisplayName("Cookie 문자열에 민감 키가 있으면 마스킹되어야 한다")
	void mask_shouldMaskCookieStringWithSensitiveKey() {
		String cookieHeader = "refresh_token=abc.def.ghi; theme=light";
		String result = apiLoggingAspect.mask(cookieHeader);
		assertThat(result).isEqualTo("\"[COOKIE_MASKED]\"");
	}

	@Test
	@DisplayName("@CookieValue 파라미터는 값을 노출하지 않아야 한다")
	void maskArgsForLog_shouldMaskCookieValueParameter() throws Exception {
		Method method = DummyController.class.getDeclaredMethod("sample", String.class, String.class);
		String result = apiLoggingAspect.maskArgsForLog(method, new Object[]{"raw-refresh-token", "plain-text"});

		assertThat(result).contains("\"[COOKIE_MASKED]\"");
		assertThat(result).contains("\"plain-text\"");
		assertThat(result).doesNotContain("raw-refresh-token");
	}

	@Test
	@DisplayName("중첩된 객체의 민감 정보도 마스킹되어야 한다")
	void mask_shouldMaskNestedSensitiveData() {
		Map<String, Object> nested = new HashMap<>();
		nested.put("password", "inner-secret");
		
		Map<String, Object> root = new HashMap<>();
		root.put("user", nested);
		root.put("token", "outer-token");

		String result = apiLoggingAspect.mask(root);

		assertThat(result).contains("\"password\":\"****\"");
		assertThat(result).contains("\"token\":\"****\"");
		assertThat(result).doesNotContain("inner-secret");
		assertThat(result).doesNotContain("outer-token");
	}

	private static final class DummyController {
		@SuppressWarnings("unused")
		void sample(@CookieValue("refresh_token") String refreshToken, String plainText) {
			// 테스트용 시그니처
		}
	}
}
