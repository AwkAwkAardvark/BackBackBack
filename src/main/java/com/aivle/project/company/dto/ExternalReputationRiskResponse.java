package com.aivle.project.company.dto;

import java.util.List;

/**
 * 외부 평판 리스크 DTO.
 */
public record ExternalReputationRiskResponse(
	Double score,
	RiskLevel label,
	List<String> topKeywords
) {
}
