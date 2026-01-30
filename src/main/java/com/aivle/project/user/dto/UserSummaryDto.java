package com.aivle.project.user.dto;

import com.aivle.project.user.entity.RoleName;
import com.aivle.project.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 요약 정보 DTO
 */
@Schema(description = "사용자 요약 정보")
public record UserSummaryDto(
	@Schema(description = "사용자 ID (UUID)", example = "a6453360-9cac-4da4-865a-964ce4ff2abf")
	String userId,

	@Schema(description = "이메일", example = "user@company.com")
	String email,

	@Schema(description = "이름", example = "홍길동")
	String name,

	@Schema(description = "권한", example = "USER")
	RoleName role
) {
	public static UserSummaryDto from(UserEntity user, RoleName role) {
		return new UserSummaryDto(
			user.getUuid().toString(),
			user.getEmail(),
			user.getName(),
			role
		);
	}
}