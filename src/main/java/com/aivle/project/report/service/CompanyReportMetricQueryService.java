package com.aivle.project.report.service;

import com.aivle.project.quarter.support.QuarterCalculator;
import com.aivle.project.metric.entity.MetricValueType;
import com.aivle.project.report.dto.ReportLatestPredictResponse;
import com.aivle.project.report.dto.ReportMetricGroupedResponse;
import com.aivle.project.report.dto.ReportMetricItemDto;
import com.aivle.project.report.dto.ReportMetricQuarterGroupDto;
import com.aivle.project.report.dto.ReportMetricRowDto;
import com.aivle.project.report.dto.ReportMetricRowProjection;
import com.aivle.project.report.dto.ReportPredictMetricItemDto;
import com.aivle.project.report.dto.ReportPredictMetricRowProjection;
import com.aivle.project.report.repository.CompanyReportMetricValuesRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 보고서 지표 조회 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyReportMetricQueryService {

	private final CompanyReportMetricValuesRepository companyReportMetricValuesRepository;
	private final com.aivle.project.report.mapper.ReportMapper reportMapper;

	public List<ReportMetricRowDto> fetchLatestMetrics(String stockCode, int fromQuarterKey, int toQuarterKey) {
		String normalizedStockCode = normalizeStockCode(stockCode);
		if (normalizedStockCode.isBlank()) {
			throw new IllegalArgumentException("stockCode가 비어 있습니다.");
		}
		validateQuarterKey(fromQuarterKey);
		validateQuarterKey(toQuarterKey);
		if (fromQuarterKey > toQuarterKey) {
			throw new IllegalArgumentException("fromQuarterKey가 toQuarterKey보다 클 수 없습니다.");
		}

		List<ReportMetricRowProjection> rows = companyReportMetricValuesRepository
			.findLatestMetricsByStockCodeAndQuarterRange(normalizedStockCode, fromQuarterKey, toQuarterKey);
		log.info(
			"보고서 지표 조회 완료: stockCode={}, fromQuarterKey={}, toQuarterKey={}, count={}",
			normalizedStockCode,
			fromQuarterKey,
			toQuarterKey,
			rows.size()
		);
		return rows.stream()
			.map(reportMapper::toRowDto)
			.toList();
	}

	public ReportMetricGroupedResponse fetchLatestMetricsGrouped(String stockCode, int fromQuarterKey, int toQuarterKey) {
		List<ReportMetricRowDto> rows = fetchLatestMetrics(stockCode, fromQuarterKey, toQuarterKey);
		String normalizedStockCode = normalizeStockCode(stockCode);
		if (rows.isEmpty()) {
			return ReportMetricGroupedResponse.empty(normalizedStockCode, fromQuarterKey, toQuarterKey);
		}

		Map<Integer, ReportMetricQuarterGroupDto> grouped = new LinkedHashMap<>();
		Map<Integer, List<ReportMetricItemDto>> items = new LinkedHashMap<>();
		for (ReportMetricRowDto row : rows) {
			items.computeIfAbsent(row.quarterKey(), key -> new ArrayList<>()).add(reportMapper.toItemDto(row));
			grouped.putIfAbsent(row.quarterKey(), new ReportMetricQuarterGroupDto(
				row.quarterKey(),
				row.versionNo(),
				row.generatedAt(),
				items.get(row.quarterKey())
			));
		}

		ReportMetricRowDto first = rows.get(0);
		return reportMapper.toGroupedResponse(
			first.corpName(),
			first.stockCode(),
			fromQuarterKey,
			toQuarterKey,
			new ArrayList<>(grouped.values())
		);
	}

	public ReportLatestPredictResponse fetchLatestPredictMetrics(String stockCode, int quarterKey) {
		String normalizedStockCode = normalizeStockCode(stockCode);
		if (normalizedStockCode.isBlank()) {
			throw new IllegalArgumentException("stockCode가 비어 있습니다.");
		}
		validateQuarterKey(quarterKey);

		List<ReportPredictMetricRowProjection> rows = companyReportMetricValuesRepository
			.findLatestMetricsByStockCodeAndQuarterKeyAndType(normalizedStockCode, quarterKey, MetricValueType.PREDICTED);
		if (rows.isEmpty()) {
			return ReportLatestPredictResponse.empty(normalizedStockCode, quarterKey);
		}

		ReportPredictMetricRowProjection first = rows.get(0);
			String downloadUrl = first.getPdfFileId() != null ? "/reports/files/" + first.getPdfFileId() : null;
		List<ReportPredictMetricItemDto> metrics = rows.stream()
			.map(reportMapper::toPredictItemDto)
			.toList();

		return reportMapper.toLatestPredictResponse(
			first.getCorpName(),
			first.getStockCode(),
			quarterKey,
			first.getVersionNo(),
			first.getGeneratedAt(),
			first.getPdfFileId(),
			first.getPdfFileName(),
			first.getPdfContentType(),
			downloadUrl,
			metrics
		);
	}

	private void validateQuarterKey(int quarterKey) {
		QuarterCalculator.parseQuarterKey(quarterKey);
	}

	private String normalizeStockCode(String stockCode) {
		if (stockCode == null) {
			return "";
		}
		String trimmed = stockCode.trim();
		if (trimmed.isBlank()) {
			return "";
		}
		if (trimmed.length() < 6) {
			return "0".repeat(6 - trimmed.length()) + trimmed;
		}
		return trimmed;
	}
}
