package com.aivle.project.report.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aivle.project.company.entity.CompaniesEntity;
import com.aivle.project.company.repository.CompaniesRepository;
import com.aivle.project.common.config.TestSecurityConfig;
import com.aivle.project.file.entity.FilesEntity;
import com.aivle.project.file.repository.FilesRepository;
import com.aivle.project.metric.entity.MetricValueType;
import com.aivle.project.report.dto.ReportPublishResult;
import com.aivle.project.report.entity.CompanyReportMetricValuesEntity;
import com.aivle.project.report.entity.CompanyReportVersionsEntity;
import com.aivle.project.report.repository.CompanyReportMetricValuesRepository;
import com.aivle.project.report.repository.CompanyReportVersionsRepository;
import com.aivle.project.report.repository.CompanyReportsRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class CompanyReportMetricPublishServiceTest {

	@Autowired
	private CompanyReportMetricPublishService companyReportMetricPublishService;

	@Autowired
	private CompaniesRepository companiesRepository;

	@Autowired
	private CompanyReportsRepository companyReportsRepository;

	@Autowired
	private CompanyReportVersionsRepository companyReportVersionsRepository;

	@Autowired
	private CompanyReportMetricValuesRepository companyReportMetricValuesRepository;

	@Autowired
	private FilesRepository filesRepository;

	@Test
	@DisplayName("보고서 지표와 PDF를 같은 버전에 저장하고 발행 처리한다")
	void publishMetrics_savesValuesAndPdf() {
		// given
		companiesRepository.save(CompaniesEntity.create(
			"00000001",
			"테스트기업",
			"TEST_CO",
			"000020",
			LocalDate.of(2025, 1, 1)
		));
		Map<String, BigDecimal> metrics = new LinkedHashMap<>();
		metrics.put("ROA", new BigDecimal("1.23"));
		metrics.put("OperatingProfitMargin", new BigDecimal("2.34"));
		metrics.put("UNKNOWN", new BigDecimal("9.99"));

		MockMultipartFile pdf = new MockMultipartFile(
			"file",
			"report.pdf",
			"application/pdf",
			"%PDF-1.4".getBytes()
		);

		// when
		ReportPublishResult result = companyReportMetricPublishService.publishMetrics(
			"000020",
			20253,
			MetricValueType.PREDICTED,
			metrics,
			pdf
		);

		// then
		assertThat(result.savedValues()).isEqualTo(2);
		assertThat(result.skippedMetrics()).isEqualTo(1);
		assertThat(result.reportVersionNo()).isEqualTo(1);
		assertThat(companyReportsRepository.count()).isEqualTo(1);
		assertThat(companyReportVersionsRepository.count()).isEqualTo(1);
		assertThat(companyReportMetricValuesRepository.count()).isEqualTo(2);
		CompanyReportVersionsEntity version = companyReportVersionsRepository.findAll().get(0);
		assertThat(version.isPublished()).isTrue();
		assertThat(version.getPdfFile()).isNotNull();
		FilesEntity savedFile = filesRepository.findAll().get(0);
		assertThat(savedFile.getId()).isEqualTo(result.pdfFileId());

		for (CompanyReportMetricValuesEntity value : companyReportMetricValuesRepository.findAll()) {
			assertThat(value.getValueType()).isEqualTo(MetricValueType.PREDICTED);
		}
	}
}
