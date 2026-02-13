package com.aivle.project.auth.token;

/**
 * Redis에 저장되는 Refresh Token 메타데이터.
 */
public record RefreshTokenCache(
	String token,
	Long userId,
	String deviceId,
	String deviceInfo,
	String ipAddress,
	long issuedAt,
	long expiresAt,
	long lastUsedAt
) {
	public RefreshTokenCache rotate(String newToken, long nowEpochMillis, long newExpiresAtMillis) {
		return new RefreshTokenCache(
			newToken,
			userId,
			deviceId,
			deviceInfo,
			ipAddress,
			nowEpochMillis,
			newExpiresAtMillis,
			nowEpochMillis
		);
	}
}
