package com.aivle.project.company.dto;

/**
 * 기업 확인 응답 DTO.
 */
public record CompanyConfirmResult(
	String companyId,
	String name,
	CompanySectorResponse sector,
	ModelStatus modelStatus,
	boolean isCached,
	String lastAnalyzedAt
) {
}
