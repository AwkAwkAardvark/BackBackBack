package com.aivle.project.dashboard.dto;

/**
 * 리스크 버킷 DTO.
 */
public record RiskStatusBucketResponse(
	String quarter,
	String dataType,
	int NORMAL,
	int CAUTION,
	int RISK
) {
}
