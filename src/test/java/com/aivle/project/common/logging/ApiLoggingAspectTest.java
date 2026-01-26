package com.aivle.project.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
