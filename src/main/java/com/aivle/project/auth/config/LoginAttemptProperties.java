package com.aivle.project.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 로그인 실패 제한 정책 설정.
 */
@Component
@ConfigurationProperties(prefix = "app.auth.login-attempt")
public class LoginAttemptProperties {

	private int maxFailures = 5;
	private Duration lockDuration = Duration.ofMinutes(15);
	private Duration failureWindow = Duration.ofMinutes(15);

	public int getMaxFailures() {
		return maxFailures;
	}

	public void setMaxFailures(int maxFailures) {
		this.maxFailures = maxFailures;
	}

	public Duration getLockDuration() {
		return lockDuration;
	}

	public void setLockDuration(Duration lockDuration) {
		this.lockDuration = lockDuration;
	}

	public Duration getFailureWindow() {
		return failureWindow;
	}

	public void setFailureWindow(Duration failureWindow) {
		this.failureWindow = failureWindow;
	}
}
