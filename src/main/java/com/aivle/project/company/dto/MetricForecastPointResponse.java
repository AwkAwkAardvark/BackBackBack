package com.aivle.project.company.dto;

/**
 * 분기별 예측 포인트 DTO.
 */
public record MetricForecastPointResponse(
	String quarter,
	Double value,
	String type
) {
}
