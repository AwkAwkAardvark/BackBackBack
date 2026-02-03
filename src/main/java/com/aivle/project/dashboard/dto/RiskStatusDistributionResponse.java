package com.aivle.project.dashboard.dto;

/**
 * 리스크 분포 DTO.
 */
public record RiskStatusDistributionResponse(
	int NORMAL,
	int CAUTION,
	int RISK
) {
}
