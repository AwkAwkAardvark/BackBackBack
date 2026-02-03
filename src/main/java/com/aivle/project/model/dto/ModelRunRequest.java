package com.aivle.project.model.dto;

/**
 * 모델 실행 요청 DTO.
 */
public record ModelRunRequest(
	String companyId,
	String companyName,
	String requestId,
	String scenario,
	Integer horizonMonths,
	String requestedBy
) {
}
