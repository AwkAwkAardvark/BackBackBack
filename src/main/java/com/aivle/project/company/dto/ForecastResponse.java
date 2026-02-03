package com.aivle.project.company.dto;

import java.util.List;

/**
 * 예측 응답 DTO.
 */
public record ForecastResponse(
	String companyId,
	String latestActualQuarter,
	String nextQuarter,
	List<MetricSeriesResponse> metricSeries,
	ModelInfoResponse modelInfo
) {
}
