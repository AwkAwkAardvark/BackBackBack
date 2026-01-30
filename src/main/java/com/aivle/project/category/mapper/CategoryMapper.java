package com.aivle.project.category.mapper;

import com.aivle.project.category.dto.CategorySummaryResponse;
import com.aivle.project.category.entity.CategoriesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

	// 엔티티의 isActive 필드를 응답의 active로 매핑한다.
	@Mapping(target = "active", source = "active")
	CategorySummaryResponse toSummaryResponse(CategoriesEntity category);
}
