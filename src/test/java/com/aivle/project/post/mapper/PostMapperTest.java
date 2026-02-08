package com.aivle.project.post.mapper;

import com.aivle.project.category.entity.CategoriesEntity;
import com.aivle.project.comment.entity.CommentsEntity;
import com.aivle.project.post.dto.PostResponse;
import com.aivle.project.post.entity.PostsEntity;
import com.aivle.project.post.entity.PostStatus;
import com.aivle.project.user.entity.RoleEntity;
import com.aivle.project.user.entity.RoleName;
import com.aivle.project.user.entity.UserEntity;
import com.aivle.project.user.entity.UserRoleEntity;
import com.aivle.project.user.entity.UserStatus;
import com.aivle.project.user.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PostMapperTest {

	@InjectMocks
	private PostMapperImpl postMapper; // MapStruct implementation

	@Mock
	private UserRoleRepository userRoleRepository;

	@Test
	@DisplayName("QnA 게시글에 관리자 답변이 없으면 qnaStatus는 pending이다")
	void toResponse_shouldReturnPendingWhenNoAdminReply() {
		// given
		CategoriesEntity qnaCategory = mock(CategoriesEntity.class);
		given(qnaCategory.getName()).willReturn("qna");

		UserEntity author = UserEntity.create("user@test.com", "pw", "user", null, UserStatus.ACTIVE);
		ReflectionTestUtils.setField(author, "id", 1L);

		PostsEntity post = PostsEntity.create(author, qnaCategory, "Title", "Content", false, PostStatus.PUBLISHED);
		ReflectionTestUtils.setField(post, "id", 100L);

		// when
		PostResponse response = postMapper.toResponse(post);

		// then
		assertThat(response.qnaStatus()).isEqualTo("pending");
	}

	@Test
	@DisplayName("QnA 게시글에 관리자 답변이 있으면 qnaStatus는 answered이다")
	void toResponse_shouldReturnAnsweredWhenAdminReplyExists() {
		// given
		CategoriesEntity qnaCategory = mock(CategoriesEntity.class);
		given(qnaCategory.getName()).willReturn("qna");

		UserEntity author = UserEntity.create("user@test.com", "pw", "user", null, UserStatus.ACTIVE);
		ReflectionTestUtils.setField(author, "id", 1L);

		PostsEntity post = PostsEntity.create(author, qnaCategory, "Title", "Content", false, PostStatus.PUBLISHED);
		ReflectionTestUtils.setField(post, "id", 100L);

		UserEntity admin = UserEntity.create("admin@test.com", "pw", "admin", null, UserStatus.ACTIVE);
		ReflectionTestUtils.setField(admin, "id", 2L);

		CommentsEntity reply = CommentsEntity.create(post, admin, null, "Reply", 0, 1);
		post.getReplies().add(reply);

		// Mock Role check
		RoleEntity adminRole = new RoleEntity(RoleName.ROLE_ADMIN, "Admin");
		given(userRoleRepository.findAllByUserId(2L)).willReturn(List.of(new UserRoleEntity(admin, adminRole)));

		// when
		PostResponse response = postMapper.toResponse(post);

		// then
		assertThat(response.qnaStatus()).isEqualTo("answered");
	}

	@Test
	@DisplayName("QnA가 아닌 게시글은 qnaStatus가 null이다")
	void toResponse_shouldReturnNullForNonQna() {
		// given
		CategoriesEntity noticeCategory = mock(CategoriesEntity.class);
		given(noticeCategory.getName()).willReturn("notices");

		UserEntity author = UserEntity.create("user@test.com", "pw", "user", null, UserStatus.ACTIVE);
		PostsEntity post = PostsEntity.create(author, noticeCategory, "Title", "Content", false, PostStatus.PUBLISHED);

		// when
		PostResponse response = postMapper.toResponse(post);

		// then
		assertThat(response.qnaStatus()).isNull();
	}
}
