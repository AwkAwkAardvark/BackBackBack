package com.aivle.project.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.aivle.project.user.entity.RoleEntity;
import com.aivle.project.user.entity.RoleName;
import com.aivle.project.user.entity.UserEntity;
import com.aivle.project.user.entity.UserRoleEntity;
import com.aivle.project.user.repository.RoleRepository;
import com.aivle.project.user.repository.UserRepository;
import com.aivle.project.user.repository.UserRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

	@InjectMocks
	private UserDomainService userDomainService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserRoleRepository userRoleRepository;

	@Test
	@DisplayName("이메일 존재 여부를 확인한다")
	void existsByEmail_shouldReturnTrueWhenUserExists() {
		// given
		given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(newEntity(UserEntity.class)));

		// when
		boolean exists = userDomainService.existsByEmail("user@test.com");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("역할이 존재하면 해당 역할로 사용자 등록을 완료한다")
	void register_shouldUseExistingRole() {
		// given
		RoleEntity role = new RoleEntity(RoleName.USER, "user role");
		given(roleRepository.findByName(RoleName.USER)).willReturn(Optional.of(role));
		given(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		UserEntity user = userDomainService.register("user@test.com", "encoded", "name", "010", RoleName.USER);

		// then
		assertThat(user.getEmail()).isEqualTo("user@test.com");
		verify(roleRepository).findByName(RoleName.USER);
		verify(roleRepository, never()).save(org.mockito.ArgumentMatchers.any(RoleEntity.class));
		verify(userRoleRepository).save(org.mockito.ArgumentMatchers.any(UserRoleEntity.class));
	}

	@Test
	@DisplayName("역할이 없으면 새 역할을 생성하고 사용자 등록을 완료한다")
	void register_shouldCreateRoleWhenMissing() {
		// given
		given(roleRepository.findByName(RoleName.ADMIN)).willReturn(Optional.empty());
		given(roleRepository.save(org.mockito.ArgumentMatchers.any(RoleEntity.class)))
			.willAnswer(invocation -> invocation.getArgument(0));
		given(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		UserEntity user = userDomainService.register("admin@test.com", "encoded", "name", "010", RoleName.ADMIN);

		// then
		assertThat(user.getEmail()).isEqualTo("admin@test.com");
		verify(roleRepository).save(org.mockito.ArgumentMatchers.any(RoleEntity.class));
		verify(userRoleRepository).save(org.mockito.ArgumentMatchers.any(UserRoleEntity.class));
	}

	private <T> T newEntity(Class<T> type) {
		try {
			var ctor = type.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("엔티티 생성에 실패했습니다", ex);
		}
	}
}
