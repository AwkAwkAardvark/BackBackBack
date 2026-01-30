package com.aivle.project.company.dto;

/**
 * 기업 검색 응답 DTO.
 */
public record CompanySearchResponse(
	Long companyId,
	String corpName,
	String corpEngName,
	String stockCode
) {
}
