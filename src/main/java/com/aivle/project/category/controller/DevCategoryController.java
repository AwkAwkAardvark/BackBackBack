package com.aivle.project.category.controller;

import com.aivle.project.category.dto.CategorySummaryResponse;
import com.aivle.project.category.repository.CategoriesRepository;
import com.aivle.project.common.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * dev 전용 카테고리 조회 API.
 */
@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
public class DevCategoryController {

	private final CategoriesRepository categoriesRepository;

	@GetMapping("/categories")
	public ApiResponse<List<CategorySummaryResponse>> list() {
		List<CategorySummaryResponse> response = categoriesRepository.findAllByDeletedAtIsNullOrderBySortOrderAscIdAsc().stream()
			.map(CategorySummaryResponse::from)
			.toList();
		return ApiResponse.ok(response);
	}
}
