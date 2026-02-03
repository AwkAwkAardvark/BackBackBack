package com.aivle.project.company.dto;

/**
 * 시그널 라이트 DTO.
 */
public record SignalLightResponse(
	String key,
	String label,
	String level,
	Double value,
	String unit,
	TooltipContentResponse tooltip
) {
}
