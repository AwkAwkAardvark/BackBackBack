package com.aivle.project.post.mapper;

import com.aivle.project.comment.entity.CommentsEntity;
import com.aivle.project.post.dto.PostResponse;
import com.aivle.project.post.dto.QaReplyResponse;
import com.aivle.project.post.entity.PostsEntity;
import com.aivle.project.user.entity.RoleName;
import com.aivle.project.user.entity.UserRoleEntity;
import com.aivle.project.user.repository.UserRoleRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class PostMapper {

	@Autowired
	protected UserRoleRepository userRoleRepository;

	// 계산된 조회수 및 isPinned 필드를 응답 규격에 맞게 매핑한다.
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "categoryId", source = "category.id")
	@Mapping(target = "viewCount", source = "viewCount")
	@Mapping(target = "isPinned", source = "pinned")
	@Mapping(target = "qnaStatus", ignore = true)
	protected abstract PostResponse toBaseResponse(PostsEntity post);

	public PostResponse toResponse(PostsEntity post) {
		PostResponse response = toBaseResponse(post);
		if (post.getCategory() != null && "qna".equalsIgnoreCase(post.getCategory().getName())) {
			String status = post.getReplies().stream()
				.anyMatch(this::isAdminComment) ? "answered" : "pending";
			
			// record의 경우 필드 수정이 불가능하므로 builder를 통해 재생성
			return PostResponse.builder()
				.id(response.id())
				.name(response.name())
				.categoryId(response.categoryId())
				.title(response.title())
				.content(response.content())
				.viewCount(response.viewCount())
				.isPinned(response.isPinned())
				.status(response.status())
				.qnaStatus(status)
				.createdAt(response.createdAt())
				.updatedAt(response.updatedAt())
				.build();
		}
		return response;
	}

	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "postId", source = "post.id")
	public abstract QaReplyResponse toQaReplyResponse(CommentsEntity comment);

	private boolean isAdminComment(CommentsEntity comment) {
		if (comment.getUser() == null) return false;
		
		List<UserRoleEntity> userRoles = userRoleRepository.findAllByUserId(comment.getUser().getId());
		return userRoles.stream()
			.anyMatch(ur -> ur.getRole().getName() == RoleName.ROLE_ADMIN);
	}
}
