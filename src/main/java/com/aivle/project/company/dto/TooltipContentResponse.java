package com.aivle.project.company.dto;

/**
 * 툴팁 설명 DTO.
 */
public record TooltipContentResponse(
	String description,
	String interpretation,
	String actionHint
) {
}
