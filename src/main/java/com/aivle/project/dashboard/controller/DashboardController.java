package com.aivle.project.dashboard.controller;

import com.aivle.project.common.dto.ApiResponse;
import com.aivle.project.dashboard.dto.DashboardSummaryResponse;
import com.aivle.project.dashboard.service.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API.
 */
@Tag(name = "대시보드", description = "대시보드 요약 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardQueryService dashboardQueryService;

	@GetMapping("/summary")
	@Operation(summary = "대시보드 요약", description = "대시보드 요약 데이터를 반환합니다.")
	public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
		@RequestParam(value = "range", required = false) String range
	) {
		DashboardSummaryResponse response = dashboardQueryService.getSummary(range);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
