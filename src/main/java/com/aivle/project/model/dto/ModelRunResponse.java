package com.aivle.project.model.dto;

/**
 * 모델 실행 응답 DTO.
 */
public record ModelRunResponse(
	String jobId,
	String status
) {
}
