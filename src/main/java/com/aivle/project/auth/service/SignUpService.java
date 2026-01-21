package com.aivle.project.auth.service;

import com.aivle.project.auth.dto.SignupRequest;
import com.aivle.project.auth.dto.SignupResponse;
import com.aivle.project.auth.exception.AuthErrorCode;
import com.aivle.project.auth.exception.AuthException;
import com.aivle.project.user.entity.RoleEntity;
import com.aivle.project.user.entity.RoleName;
import com.aivle.project.user.entity.UserEntity;
import com.aivle.project.user.entity.UserRoleEntity;
import com.aivle.project.user.entity.UserStatus;
import com.aivle.project.user.repository.RoleRepository;
import com.aivle.project.user.repository.UserRepository;
import com.aivle.project.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 처리.
 */
@Service
@RequiredArgsConstructor
public class SignUpService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}

		String encodedPassword = passwordEncoder.encode(request.getPassword());
		UserEntity user = UserEntity.create(
			request.getEmail(),
			encodedPassword,
			request.getName(),
			request.getPhone(),
			UserStatus.ACTIVE
		);
		userRepository.save(user);

		RoleEntity role = roleRepository.findByName(RoleName.USER)
			.orElseGet(() -> roleRepository.save(new RoleEntity(RoleName.USER, "user role")));
		userRoleRepository.save(new UserRoleEntity(user, role));

		return SignupResponse.of(user, RoleName.USER);
	}
}
