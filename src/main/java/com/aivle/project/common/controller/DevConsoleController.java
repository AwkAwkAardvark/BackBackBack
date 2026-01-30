package com.aivle.project.common.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * dev 환경 API 점검용 콘솔 페이지.
 */
@Profile("dev")
@Controller
@RequestMapping("/dev")
@Tag(name = "개발", description = "개발용 콘솔 페이지")
public class DevConsoleController {

	@GetMapping("/console")
	@Operation(hidden = true)
	public String console() {
		return "api-console";
	}

	@GetMapping("/file-console")
	@Operation(hidden = true)
	public String fileConsole() {
		return "file-console";
	}

	@GetMapping("/report-predict-console")
	@Operation(hidden = true)
	public String reportPredictConsole() {
		return "report-predict-console";
	}
}
