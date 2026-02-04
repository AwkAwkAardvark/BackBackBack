package com.aivle.project.company.dto;

/**
 * 기업 요약 DTO.
 */
public record CompanySummaryResponse(
	String id,
	String name,
	CompanySectorResponse sector,
	double overallScore,
	String lastUpdatedAt,
	CompanyKpiMiniResponse kpi
) {
}
