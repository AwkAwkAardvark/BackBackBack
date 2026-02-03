package com.aivle.project.company.dto;

import java.util.List;

/**
 * 지표 시계열 DTO.
 */
public record MetricSeriesResponse(
	String key,
	String label,
	String unit,
	List<MetricForecastPointResponse> points
) {
}
