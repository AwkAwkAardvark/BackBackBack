package com.aivle.project.company.controller;

import com.aivle.project.common.dto.ApiResponse;
import com.aivle.project.company.dto.CompanyConfirmRequest;
import com.aivle.project.company.dto.CompanyConfirmResult;
import com.aivle.project.company.dto.CompanyOverviewResponse;
import com.aivle.project.company.dto.CompanySummaryResponse;
import com.aivle.project.company.dto.UpdateRequestCreateRequest;
import com.aivle.project.company.dto.UpdateRequestCreateResponse;
import com.aivle.project.company.service.CompanyQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기업 조회 API.
 */
@Tag(name = "기업", description = "기업 조회 및 확인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {

	private final CompanyQueryService companyQueryService;

	@GetMapping
	@Operation(summary = "기업 목록 조회", description = "기업 목록을 조회합니다.")
	public ResponseEntity<ApiResponse<List<CompanySummaryResponse>>> listCompanies(
		@RequestParam(value = "q", required = false) String query,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size
	) {
		List<CompanySummaryResponse> response = companyQueryService.listCompanies(query, page, size);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@GetMapping("/{companyId}")
	@Operation(summary = "기업 요약 조회", description = "기업 요약 정보를 조회합니다.")
	public ResponseEntity<ApiResponse<CompanySummaryResponse>> getCompanySummary(
		@PathVariable("companyId") String companyId
	) {
		CompanySummaryResponse response = companyQueryService.getCompanySummary(companyId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@GetMapping("/{companyId}/overview")
	@Operation(summary = "기업 상세 조회", description = "기업 상세 정보를 조회합니다.")
	public ResponseEntity<ApiResponse<CompanyOverviewResponse>> getCompanyOverview(
		@PathVariable("companyId") String companyId
	) {
		CompanyOverviewResponse response = companyQueryService.getCompanyOverview(companyId);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping("/confirm")
	@Operation(summary = "기업 확인", description = "기업 확인 결과를 반환합니다.")
	public ResponseEntity<ApiResponse<CompanyConfirmResult>> confirmCompany(
		@RequestBody CompanyConfirmRequest request
	) {
		CompanyConfirmResult response = companyQueryService.confirmCompany(request);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping("/{companyId}/update-requests")
	@Operation(summary = "기업 업데이트 요청", description = "기업 업데이트 요청을 등록합니다.")
	public ResponseEntity<ApiResponse<UpdateRequestCreateResponse>> createUpdateRequest(
		@PathVariable("companyId") String companyId,
		@RequestBody(required = false) UpdateRequestCreateRequest request
	) {
		UpdateRequestCreateResponse response = companyQueryService.createUpdateRequest(companyId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}
}
