package com.aivle.project.report.dto;

import com.aivle.project.metric.entity.MetricValueType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 보고서 지표 응답 DTO.
 */
public record ReportMetricRowDto(
	String corpName,
	String stockCode,
	String metricCode,
	String metricNameKo,
	BigDecimal metricValue,
	MetricValueType valueType,
	int quarterKey,
	int versionNo,
	LocalDateTime generatedAt
) {
}
