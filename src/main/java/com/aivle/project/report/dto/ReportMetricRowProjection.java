package com.aivle.project.report.dto;

import com.aivle.project.metric.entity.MetricValueType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 보고서 지표 조회 응답 프로젝션.
 */
public interface ReportMetricRowProjection {

	String getCorpName();

	String getStockCode();

	String getMetricCode();

	String getMetricNameKo();

	BigDecimal getMetricValue();

	MetricValueType getValueType();

	int getQuarterKey();

	int getVersionNo();

	LocalDateTime getGeneratedAt();
}
