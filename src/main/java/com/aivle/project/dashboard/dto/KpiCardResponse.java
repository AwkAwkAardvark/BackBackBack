package com.aivle.project.dashboard.dto;

import com.aivle.project.company.dto.DeltaValueResponse;
import com.aivle.project.company.dto.TooltipContentResponse;

/**
 * KPI 카드 DTO.
 */
public record KpiCardResponse(
	String key,
	String title,
	Object value,
	String unit,
	String tone,
	DeltaValueResponse delta,
	KpiBadgeResponse badge,
	TooltipContentResponse tooltip
) {
}
