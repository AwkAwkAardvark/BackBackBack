package com.aivle.project.dashboard.dto;

import java.util.List;

/**
 * 대시보드 요약 응답 DTO.
 */
public record DashboardSummaryResponse(
	String range,
	List<KpiCardResponse> kpis,
	String latestActualQuarter,
	String forecastQuarter,
	List<String> windowQuarters,
	RiskStatusDistributionResponse riskStatusDistribution,
	List<RiskStatusBucketResponse> riskStatusDistributionTrend
) {
}
