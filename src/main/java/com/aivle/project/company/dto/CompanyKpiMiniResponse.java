package com.aivle.project.company.dto;

/**
 * 기업 KPI 요약 DTO.
 */
public record CompanyKpiMiniResponse(
	Double networkHealth,
	Double annualRevenue
) {
}
