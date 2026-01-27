package com.aivle.project.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

	@NotBlank(message = "이메일은 필수입니다.")
	private String email;

	@NotBlank(message = "기존 비밀번호는 필수입니다.")
	private String oldPassword;

	@NotBlank(message = "새 비밀번호는 필수입니다.")
	private String newPassword;
}
