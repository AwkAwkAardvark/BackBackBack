package com.aivle.project.category.dto;

import com.aivle.project.category.entity.CategoriesEntity;

/**
 * 카테고리 요약 응답 DTO.
 */
public record CategorySummaryResponse(
	Long id,
	String name,
	String description,
	int sortOrder,
	boolean active
) {
	public static CategorySummaryResponse from(CategoriesEntity category) {
		return new CategorySummaryResponse(
			category.getId(),
			category.getName(),
			category.getDescription(),
			category.getSortOrder(),
			category.isActive()
		);
	}
}
