package com.aivle.project.company.dto;

/**
 * 기업 확인 요청 DTO.
 */
public record CompanyConfirmRequest(
	String companyId,
	String code,
	String name
) {
}
