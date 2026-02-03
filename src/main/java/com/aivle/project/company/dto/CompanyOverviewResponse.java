package com.aivle.project.company.dto;

import java.util.List;

/**
 * 기업 상세 요약 DTO.
 */
public record CompanyOverviewResponse(
	CompanySummaryResponse company,
	ForecastResponse forecast,
	List<MetricItemResponse> keyMetrics,
	List<SignalLightResponse> signals,
	String aiComment,
	ExternalReputationRiskResponse externalReputationRisk,
	ModelStatus modelStatus
) {
}
