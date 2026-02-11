package com.aivle.project.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aivle.project.auth.entity.RefreshTokenEntity;
import com.aivle.project.auth.repository.RefreshTokenRepository;
import com.aivle.project.auth.token.JwtTokenService;
import com.aivle.project.auth.token.RefreshTokenCache;
import com.aivle.project.user.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	private static final Long USER_ID = 1L;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private JwtTokenService jwtTokenService;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private SetOperations<String, String> setOperations;

	@Captor
	private ArgumentCaptor<String> valueCaptor;

	@Captor
	private ArgumentCaptor<Duration> durationCaptor;

	private RefreshTokenService refreshTokenService;

	@BeforeEach
	void setUp() {
		ObjectMapper objectMapper = new ObjectMapper();
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
		refreshTokenService = new RefreshTokenService(redisTemplate, objectMapper, refreshTokenRepository, jwtTokenService);
	}

	@Test
	@DisplayName("리프레시 토큰 저장 시 Redis와 DB에 모두 기록한다")
	void storeToken_shouldPersistToRedisAndDatabase() throws Exception {
		// given: 사용자 정보와 만료 시간 설정을 준비
		CustomUserDetails userDetails = mock(CustomUserDetails.class);
		when(userDetails.getId()).thenReturn(USER_ID);
		when(jwtTokenService.getRefreshTokenExpirationSeconds()).thenReturn(600L);

		// when: 리프레시 토큰을 저장
		refreshTokenService.storeToken(userDetails, "rt-1", "device-1", "ios", "127.0.0.1");

		// then: Redis 저장과 DB 저장이 수행된다
		verify(valueOperations).set(eq("refresh:rt-1"), valueCaptor.capture(), durationCaptor.capture());
		RefreshTokenCache cache = new ObjectMapper().readValue(valueCaptor.getValue(), RefreshTokenCache.class);
		assertThat(cache.token()).isEqualTo("rt-1");
		assertThat(cache.userId()).isEqualTo(USER_ID);
		assertThat(durationCaptor.getValue().getSeconds()).isPositive();

		ArgumentCaptor<RefreshTokenEntity> entityCaptor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
		verify(refreshTokenRepository).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getTokenValue()).isEqualTo("rt-1");
		assertThat(entityCaptor.getValue().getUserId()).isEqualTo(USER_ID);
	}

	@Test
	@DisplayName("Redis 미스 시 DB에서 로드하고 캐시를 복구한다")
	void loadValidToken_shouldFallbackToDatabaseAndRehydrateCache() {
		// given: Redis 미스와 DB에 존재하는 토큰 상태를 준비
		LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
		RefreshTokenEntity entity = new RefreshTokenEntity(USER_ID, "rt-2", "android", "127.0.0.1", expiresAt);
		ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now().minusMinutes(5));

		when(valueOperations.get("refresh:rt-2")).thenReturn(null);
		when(refreshTokenRepository.findByTokenValue("rt-2")).thenReturn(Optional.of(entity));

		// when: 유효한 토큰을 조회
		RefreshTokenCache cache = refreshTokenService.loadValidToken("rt-2");

		// then: 캐시가 재구성된다
		assertThat(cache.token()).isEqualTo("rt-2");
		assertThat(cache.userId()).isEqualTo(USER_ID);
		assertThat(cache.deviceId()).isEqualTo("default");
		verify(valueOperations).set(eq("refresh:rt-2"), any(String.class), any(Duration.class));
		verify(setOperations).add("sessions:" + USER_ID, "rt-2");
	}

	@Test
	@DisplayName("리프레시 회전 시 기존 토큰이 폐기되고 신규 토큰이 저장된다")
	void rotateToken_shouldRevokeOldAndStoreNew() throws Exception {
		// given: 기존 토큰이 Redis와 DB에 존재하는 상태를 준비
		RefreshTokenCache existing = new RefreshTokenCache(
			"rt-old",
			USER_ID,
			"device-3",
			"ios",
			"127.0.0.1",
			1L,
			System.currentTimeMillis() / 1000 + 600,
			1L
		);
		String json = new ObjectMapper().writeValueAsString(existing);
		when(valueOperations.get("refresh:rt-old")).thenReturn(json);
		when(jwtTokenService.getRefreshTokenExpirationSeconds()).thenReturn(600L);

		RefreshTokenEntity entity = new RefreshTokenEntity(USER_ID, "rt-old", "ios", "127.0.0.1", LocalDateTime.now().plusDays(1));
		when(refreshTokenRepository.findByTokenValue("rt-old")).thenReturn(Optional.of(entity));

		// when: 리프레시 토큰을 회전
		refreshTokenService.rotateToken("rt-old", "rt-new");

		// then: 기존 토큰 삭제와 신규 토큰 저장이 수행된다
		verify(redisTemplate).delete("refresh:rt-old");
		verify(setOperations).remove("sessions:" + USER_ID, "rt-old");
		verify(valueOperations).set(eq("refresh:rt-new"), any(String.class), any(Duration.class));
		verify(refreshTokenRepository, atLeastOnce()).save(any(RefreshTokenEntity.class));
	}

	@Test
	@DisplayName("초 단위 레거시 캐시 토큰도 유효하면 정상 로드된다")
	void loadValidToken_shouldSupportLegacySecondEpochCache() throws Exception {
		// given: 레거시(초 단위) 캐시 토큰이 Redis에 저장되어 있다
		long nowSeconds = System.currentTimeMillis() / 1000;
		RefreshTokenCache legacyCache = new RefreshTokenCache(
			"legacy-rt",
			USER_ID,
			"device-1",
			"ios",
			"127.0.0.1",
			nowSeconds - 10,
			nowSeconds + 600,
			nowSeconds - 10
		);
		String legacyJson = new ObjectMapper().writeValueAsString(legacyCache);
		when(valueOperations.get("refresh:legacy-rt")).thenReturn(legacyJson);

		// when
		RefreshTokenCache loaded = refreshTokenService.loadValidToken("legacy-rt");

		// then
		assertThat(loaded.token()).isEqualTo("legacy-rt");
		assertThat(loaded.userId()).isEqualTo(USER_ID);
	}

	@Test
	@DisplayName("전체 로그아웃 시 사용자 리프레시 토큰을 Redis/DB에서 모두 폐기한다")
	void revokeAllByUserId_shouldRevokeAllUserTokens() {
		// given: 사용자 세션 키에 토큰들이 존재하고 DB에 활성 토큰이 있다
		RefreshTokenEntity first = new RefreshTokenEntity(USER_ID, "rt-1", "ios", "127.0.0.1", LocalDateTime.now().plusDays(1));
		RefreshTokenEntity second = new RefreshTokenEntity(USER_ID, "rt-2", "android", "127.0.0.1", LocalDateTime.now().plusDays(1));
		when(setOperations.members("sessions:" + USER_ID)).thenReturn(Set.of("rt-1", "rt-2"));
		when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(USER_ID)).thenReturn(List.of(first, second));

		// when: 전체 토큰 폐기
		refreshTokenService.revokeAllByUserId(USER_ID);

		// then: Redis 토큰 키/세션 키가 삭제되고 DB 토큰이 revoke 처리된다
		verify(redisTemplate).delete("refresh:rt-1");
		verify(redisTemplate).delete("refresh:rt-2");
		verify(redisTemplate).delete("sessions:" + USER_ID);
		verify(refreshTokenRepository).saveAll(List.of(first, second));
		assertThat(first.isRevoked()).isTrue();
		assertThat(second.isRevoked()).isTrue();
	}
}
