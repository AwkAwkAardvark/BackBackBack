package com.aivle.project.company.dto;

import java.util.List;

/**
 * 기업 검색 응답 DTO.
 */
public record CompanySearchResponse(
	List<CompanySearchItemResponse> items,
	int count
) {
}
