package com.aivle.project.comment.mapper;

import com.aivle.project.comment.dto.CommentResponse;
import com.aivle.project.comment.entity.CommentsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

	// 연관 엔티티의 식별자를 응답 필드로 평탄화한다.
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "postId", source = "post.id")
	@Mapping(target = "parentId", source = "parent.id")
	CommentResponse toResponse(CommentsEntity comment);
}
