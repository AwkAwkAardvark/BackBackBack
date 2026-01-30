package com.aivle.project.company.controller;

import com.aivle.project.common.dto.ApiResponse;
import com.aivle.project.common.error.CommonErrorCode;
import com.aivle.project.common.error.CommonException;
import com.aivle.project.company.batch.DartCorpCodeJobService;
import com.aivle.project.company.dto.DartCorpSyncResponse;
import com.aivle.project.company.mapper.DartCorpSyncMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DART 기업 목록 동기화 수동 실행 API.
 */
@Tag(name = "기업 동기화", description = "DART 기업 목록 동기화 배치 실행")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dart/corp-sync")
public class DartCorpSyncController {

	private final DartCorpCodeJobService jobService;
	private final DartCorpSyncMapper dartCorpSyncMapper;

	@PostMapping
	@Operation(summary = "DART 기업 목록 동기화 실행", description = "관리자용 수동 실행 API")
	public ResponseEntity<ApiResponse<DartCorpSyncResponse>> triggerSync() {
		try {
			JobExecution execution = jobService.launch("manual");
			DartCorpSyncResponse response = dartCorpSyncMapper.toResponse(execution);
			return ResponseEntity.accepted().body(ApiResponse.ok(response));
		} catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException ex) {
			throw new CommonException(CommonErrorCode.COMMON_409);
		} catch (JobRestartException | JobParametersInvalidException ex) {
			throw new CommonException(CommonErrorCode.COMMON_400);
		}
	}
}
