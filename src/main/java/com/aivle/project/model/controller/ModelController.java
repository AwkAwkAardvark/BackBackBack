package com.aivle.project.model.controller;

import com.aivle.project.common.dto.ApiResponse;
import com.aivle.project.model.dto.ModelRunRequest;
import com.aivle.project.model.dto.ModelRunResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 모델 실행 API.
 */
@Tag(name = "모델", description = "모델 실행 API")
@RestController
@RequestMapping("/api/model")
public class ModelController {

	@PostMapping("/run")
	@Operation(summary = "모델 실행 요청", description = "모델 실행 작업을 요청합니다.")
	public ResponseEntity<ApiResponse<ModelRunResponse>> runModel(@RequestBody ModelRunRequest request) {
		ModelRunResponse response = new ModelRunResponse(UUID.randomUUID().toString(), "queued");
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
	}
}
