package com.aivle.project.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.aivle.project.auth.token.JwtProperties;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class AccessTokenBlacklistServiceTest {

	private AccessTokenBlacklistService accessTokenBlacklistService;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private JwtProperties jwtProperties;

	@BeforeEach
	void setUp() {
		jwtProperties = new JwtProperties();
		jwtProperties.getAccessToken().setExpiration(3600);
		accessTokenBlacklistService = new AccessTokenBlacklistService(redisTemplate, jwtProperties);
	}

	@Test
	@DisplayName("블랙리스트 등록 시 TTL과 함께 저장된다")
	void blacklist_shouldStoreWithTtl() {
		// given
		String jti = "token-id";
		Instant expiresAt = Instant.now().plusSeconds(120);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		accessTokenBlacklistService.blacklist(jti, expiresAt);

		// then
		ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
		verify(valueOperations).set(
			org.mockito.ArgumentMatchers.eq("blacklist:access:" + jti),
			org.mockito.ArgumentMatchers.eq("1"),
			ttlCaptor.capture()
		);
		assertThat(ttlCaptor.getValue().toSeconds()).isBetween(1L, 120L);
	}

	@Test
	@DisplayName("만료된 토큰은 블랙리스트에 저장하지 않는다")
	void blacklist_shouldSkipWhenExpired() {
		// given
		String jti = "expired-token";
		Instant expiresAt = Instant.now().minusSeconds(10);

		// when
		accessTokenBlacklistService.blacklist(jti, expiresAt);

		// then
		verify(valueOperations, never()).set(
			org.mockito.ArgumentMatchers.anyString(),
			org.mockito.ArgumentMatchers.anyString(),
			org.mockito.ArgumentMatchers.any(Duration.class)
		);
	}

	@Test
	@DisplayName("전체 로그아웃 기준 시각을 저장한다")
	void markLogoutAll_shouldStoreLogoutAt() {
		// given
		String userId = "user-1";
		Instant logoutAt = Instant.now();
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		accessTokenBlacklistService.markLogoutAll(userId, logoutAt);

		// then
		verify(valueOperations).set(
			org.mockito.ArgumentMatchers.eq("logout-all:" + userId),
			org.mockito.ArgumentMatchers.eq(String.valueOf(logoutAt.getEpochSecond())),
			org.mockito.ArgumentMatchers.eq(Duration.ofSeconds(3600))
		);
	}

	@Test
	@DisplayName("블랙리스트 여부를 Redis에서 조회한다")
	void isBlacklisted_shouldCheckRedis() {
		// given
		given(redisTemplate.hasKey("blacklist:access:token")).willReturn(true);

		// when
		boolean result = accessTokenBlacklistService.isBlacklisted("token");

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("전체 로그아웃 기준 시각을 조회한다")
	void getLogoutAllAt_shouldReturnInstant() {
		// given
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get("logout-all:user-1")).willReturn("1700000000");

		// when
		Instant result = accessTokenBlacklistService.getLogoutAllAt("user-1");

		// then
		assertThat(result).isEqualTo(Instant.ofEpochSecond(1700000000));
	}

	@Test
	@DisplayName("전체 로그아웃 기준 시각이 잘못된 값이면 null을 반환한다")
	void getLogoutAllAt_shouldReturnNullWhenInvalid() {
		// given
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get("logout-all:user-1")).willReturn("invalid");

		// when
		Instant result = accessTokenBlacklistService.getLogoutAllAt("user-1");

		// then
		assertThat(result).isNull();
	}
}
