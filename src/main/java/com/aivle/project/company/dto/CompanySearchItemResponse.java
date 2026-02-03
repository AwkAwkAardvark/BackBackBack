package com.aivle.project.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 기업 검색 항목 DTO.
 */
public record CompanySearchItemResponse(
	Long companyId,
	@JsonProperty("corpName")
	String name,
	@JsonProperty("stockCode")
	String code,
	CompanySectorResponse sector
) {
}
