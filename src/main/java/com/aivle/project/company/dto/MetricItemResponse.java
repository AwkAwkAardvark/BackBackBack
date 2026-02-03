package com.aivle.project.company.dto;

/**
 * 지표 항목 DTO.
 */
public record MetricItemResponse(
	String key,
	String label,
	Double value,
	String unit,
	DeltaValueResponse delta,
	TooltipContentResponse tooltip
) {
}
