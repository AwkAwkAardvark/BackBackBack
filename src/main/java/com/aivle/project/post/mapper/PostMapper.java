package com.aivle.project.post.mapper;

import com.aivle.project.post.dto.PostResponse;
import com.aivle.project.post.entity.PostsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {

	// 계산된 조회수 및 isPinned 필드를 응답 규격에 맞게 매핑한다.
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "categoryId", source = "category.id")
	@Mapping(target = "viewCount", source = "viewCount")
	@Mapping(target = "isPinned", source = "pinned")
	PostResponse toResponse(PostsEntity post);
}
