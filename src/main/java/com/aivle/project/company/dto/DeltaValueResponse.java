package com.aivle.project.company.dto;

/**
 * 증감 DTO.
 */
public record DeltaValueResponse(
	Double value,
	String unit,
	String direction,
	String label
) {
}
