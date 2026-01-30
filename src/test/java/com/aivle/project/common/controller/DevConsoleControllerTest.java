package com.aivle.project.common.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aivle.project.common.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(TestSecurityConfig.class)
class DevConsoleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("개발 콘솔에 보고서 지표 업로드 섹션이 노출된다")
	void console_shouldContainReportImportSection() throws Exception {
		mockMvc.perform(get("/dev/console"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("보고서 지표 업로드")))
			.andExpect(content().string(containsString("report-import")));
	}
}
