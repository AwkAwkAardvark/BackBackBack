package com.aivle.project.category.controller;

import com.aivle.project.category.dto.CategorySummaryResponse;
import com.aivle.project.category.repository.CategoriesRepository;
import com.aivle.project.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "개발", description = "개발용 카테고리 API")
public class DevCategoryController {

	private final CategoriesRepository categoriesRepository;
	private final com.aivle.project.category.mapper.CategoryMapper categoryMapper;

	@GetMapping("/categories")
	@Operation(summary = "개발용 카테고리 조회", description = "개발 환경에서 카테고리를 조회합니다.", security = {})
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
	})
	public ApiResponse<List<CategorySummaryResponse>> list() {
		List<CategorySummaryResponse> response = categoriesRepository.findAllByDeletedAtIsNullOrderBySortOrderAscIdAsc().stream()
			.map(categoryMapper::toSummaryResponse)
			.toList();
		return ApiResponse.ok(response);
	}
}
