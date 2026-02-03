package com.aivle.project.company.dto;

/**
 * 기업 요약 DTO.
 */
public record CompanySummaryResponse(
	String id,
	String name,
	CompanySectorResponse sector,
	double overallScore,
	RiskLevel riskLevel,
	String lastUpdatedAt,
	CompanyKpiMiniResponse kpi
) {
}
