package com.aivle.project.dashboard.service;

import com.aivle.project.dashboard.dto.DashboardSummaryResponse;
import com.aivle.project.dashboard.dto.RiskStatusDistributionResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 대시보드 조회 서비스.
 */
@Service
public class DashboardQueryService {

	public DashboardSummaryResponse getSummary(String range) {
		String resolvedRange = (range == null || range.isBlank()) ? "30d" : range;
		String currentQuarter = resolveCurrentQuarter();
		return new DashboardSummaryResponse(
			resolvedRange,
			List.of(),
			currentQuarter,
			currentQuarter,
			List.of(),
			new RiskStatusDistributionResponse(0, 0, 0),
			List.of()
		);
	}

	private String resolveCurrentQuarter() {
		LocalDate today = LocalDate.now();
		int quarter = (today.getMonthValue() - 1) / 3 + 1;
		return today.getYear() + "Q" + quarter;
	}
}
