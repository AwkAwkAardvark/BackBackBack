package com.aivle.project.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserRoleRepository userRoleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Captor
	private ArgumentCaptor<UserEntity> userCaptor;

	@Test
	@DisplayName("회원가입 시 사용자와 USER 역할을 저장한다")
	void signup_shouldPersistUserAndRole() {
		// given: 회원가입 요청과 역할 정보를 준비
		SignUpService signUpService = new SignUpService(userRepository, roleRepository, userRoleRepository, passwordEncoder);
		SignupRequest request = new SignupRequest();
		request.setEmail("new@test.com");
		request.setPassword("password123");
		request.setName("tester");
		request.setPhone("01012345678");

		when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password123")).thenReturn("encoded");
		when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(new RoleEntity(RoleName.USER, "user role")));

		// when: 회원가입을 수행
		SignupResponse response = signUpService.signup(request);

		// then: 사용자 저장과 매핑이 수행된다
		verify(userRepository).save(userCaptor.capture());
		UserEntity saved = userCaptor.getValue();
		assertThat(saved.getEmail()).isEqualTo("new@test.com");
		assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(response.email()).isEqualTo("new@test.com");
		verify(userRoleRepository).save(org.mockito.ArgumentMatchers.any(UserRoleEntity.class));
	}

	@Test
	@DisplayName("역할이 없으면 USER 역할을 생성한다")
	void signup_shouldCreateRoleWhenMissing() {
		// given: 역할이 없는 상태를 준비
		SignUpService signUpService = new SignUpService(userRepository, roleRepository, userRoleRepository, passwordEncoder);
		SignupRequest request = new SignupRequest();
		request.setEmail("role@test.com");
		request.setPassword("password123");
		request.setName("tester");

		when(userRepository.findByEmail("role@test.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password123")).thenReturn("encoded");
		when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());
		when(roleRepository.save(org.mockito.ArgumentMatchers.any(RoleEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		// when: 회원가입을 수행
		signUpService.signup(request);

		// then: 역할 생성이 수행된다
		verify(roleRepository).save(org.mockito.ArgumentMatchers.any(RoleEntity.class));
	}

	@Test
	@DisplayName("이미 존재하는 이메일이면 회원가입이 실패한다")
	void signup_shouldFailWhenEmailExists() {
		// given: 기존 사용자 이메일을 준비
		SignUpService signUpService = new SignUpService(userRepository, roleRepository, userRoleRepository, passwordEncoder);
		SignupRequest request = new SignupRequest();
		request.setEmail("dup@test.com");
		request.setPassword("password123");
		request.setName("tester");

		when(userRepository.findByEmail("dup@test.com")).thenReturn(Optional.of(org.mockito.Mockito.mock(UserEntity.class)));

		// when & then: 예외가 발생한다
		assertThatThrownBy(() -> signUpService.signup(request))
			.isInstanceOf(AuthException.class)
			.hasMessage(AuthErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
	}
}
