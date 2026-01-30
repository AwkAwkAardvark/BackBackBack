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

	public static ReportMetricRowDto from(ReportMetricRowProjection projection) {
		return new ReportMetricRowDto(
			projection.getCorpName(),
			projection.getStockCode(),
			projection.getMetricCode(),
			projection.getMetricNameKo(),
			projection.getMetricValue(),
			projection.getValueType(),
			projection.getQuarterKey(),
			projection.getVersionNo(),
			projection.getGeneratedAt()
		);
	}
}
