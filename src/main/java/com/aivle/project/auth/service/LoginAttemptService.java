package com.aivle.project.auth.service;

import com.aivle.project.auth.config.LoginAttemptProperties;
import com.aivle.project.auth.exception.AuthErrorCode;
import com.aivle.project.auth.exception.AuthException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 로그인 실패 횟수/잠금 상태 관리 서비스.
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

	private static final String FAIL_COUNT_KEY = "login:fail-count:%s";
	private static final String LOCK_KEY = "login:lock:%s";

	private final StringRedisTemplate redisTemplate;
	private final LoginAttemptProperties properties;

	public void validateNotLocked(String email) {
		if (isLocked(email)) {
			throw new AuthException(AuthErrorCode.LOGIN_ATTEMPT_LIMITED);
		}
	}

	public boolean recordFailure(String email) {
		String normalizedEmail = normalizeEmail(email);
		String failKey = failCountKey(normalizedEmail);
		Long count = redisTemplate.opsForValue().increment(failKey);
		if (count == null) {
			return false;
		}
		if (count == 1) {
			redisTemplate.expire(failKey, properties.getFailureWindow());
		}
		if (count >= properties.getMaxFailures()) {
			redisTemplate.opsForValue().set(lockKey(normalizedEmail), "1", properties.getLockDuration());
			redisTemplate.delete(failKey);
			return true;
		}
		return false;
	}

	public void clearFailures(String email) {
		String normalizedEmail = normalizeEmail(email);
		redisTemplate.delete(failCountKey(normalizedEmail));
	}

	private boolean isLocked(String email) {
		String normalizedEmail = normalizeEmail(email);
		return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(normalizedEmail)));
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return "";
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String failCountKey(String email) {
		return String.format(FAIL_COUNT_KEY, email);
	}

	private String lockKey(String email) {
		return String.format(LOCK_KEY, email);
	}
}
