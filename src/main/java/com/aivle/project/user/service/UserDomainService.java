package com.aivle.project.user.service;

import com.aivle.project.user.entity.RoleEntity;
import com.aivle.project.user.entity.RoleName;
import com.aivle.project.user.entity.UserEntity;
import com.aivle.project.user.entity.UserRoleEntity;
import com.aivle.project.user.entity.UserStatus;
import com.aivle.project.user.repository.RoleRepository;
import com.aivle.project.user.repository.UserRepository;
import com.aivle.project.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 도메인 로직을 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;

	public boolean existsByEmail(String email) {
		return userRepository.findByEmail(email).isPresent();
	}

	@Transactional
	public UserEntity register(String email, String encodedPassword, String name, String phone, RoleName roleName) {
		UserEntity user = UserEntity.create(
			email,
			encodedPassword,
			name,
			phone,
			UserStatus.PENDING  // 이메일 인증 대기 상태
		);
		userRepository.save(user);

		RoleEntity role = roleRepository.findByName(roleName)
			.orElseGet(() -> roleRepository.save(new RoleEntity(roleName, roleName.name().toLowerCase() + " role")));
		userRoleRepository.save(new UserRoleEntity(user, role));

		return user;
	}

	/**
	 * 사용자 활성화 (이메일 인증 완료 시).
	 */
	@Transactional
	public void activateUser(Long userId) {
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);
	}

	/**
	 * 사용자 조회.
	 */
	public UserEntity getUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
	}

	/**
	 * 사용자 조회 (Email).
	 */
	public UserEntity getUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
	}

	@Transactional
	public void updatePassword(String email, String encodedPassword) {
		UserEntity user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
		user.changePassword(encodedPassword);
	}
}
