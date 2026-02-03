package com.aivle.project.company.controller;

import com.aivle.project.common.dto.ApiResponse;
import com.aivle.project.company.dto.CompanySearchItemResponse;
import com.aivle.project.company.service.CompanySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기업 검색 API.
 */
@Tag(name = "기업", description = "기업 검색")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanySearchController {

	private final CompanySearchService companySearchService;

	@GetMapping("/search")
	@Operation(summary = "기업명 검색", description = "기업명/영문명 부분 일치로 기업을 검색합니다.")
	public ResponseEntity<ApiResponse<List<CompanySearchItemResponse>>> searchCompanies(
		@Parameter(description = "기업명", example = "삼성")
		@RequestParam(value = "name", required = false) String name,
		@Parameter(description = "검색 키워드(이전 호환)", example = "삼성")
		@RequestParam(value = "keyword", required = false) String keyword,
		@Parameter(description = "종목 코드", example = "005930")
		@RequestParam(value = "code", required = false) String code,
		@Parameter(description = "최대 반환 개수", example = "10")
		@RequestParam(value = "limit", required = false) Integer limit
	) {
		String resolvedName = StringUtils.hasText(name) ? name : keyword;
		List<CompanySearchItemResponse> items = resolveSearchItems(resolvedName, code, limit);
		return ResponseEntity.ok(ApiResponse.ok(items));
	}

	private List<CompanySearchItemResponse> resolveSearchItems(String name, String code, Integer limit) {
		if (code != null && !code.isBlank()) {
			Optional<CompanySearchItemResponse> found = companySearchService.searchByCode(code);
			return found.map(List::of).orElseGet(List::of);
		}
		return companySearchService.searchByName(name, limit);
	}
}
