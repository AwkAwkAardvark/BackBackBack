package com.aivle.project.report.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aivle.project.company.entity.CompaniesEntity;
import com.aivle.project.company.repository.CompaniesRepository;
import com.aivle.project.common.config.TestSecurityConfig;
import com.aivle.project.metric.entity.MetricValueType;
import com.aivle.project.report.dto.ReportPredictRequest;
import com.aivle.project.report.dto.ReportPredictResult;
import com.aivle.project.report.entity.CompanyReportMetricValuesEntity;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class CompanyReportMetricPredictServiceTest {

	@Autowired
	private CompanyReportMetricPredictService companyReportMetricPredictService;

	@Autowired
	private CompaniesRepository companiesRepository;

	@Autowired
	private CompanyReportsRepository companyReportsRepository;

	@Autowired
	private CompanyReportVersionsRepository companyReportVersionsRepository;

	@Autowired
	private CompanyReportMetricValuesRepository companyReportMetricValuesRepository;

	@Test
	@DisplayName("예측값 요청을 저장하고 PREDICTED로 적재한다")
	void importPredictedMetrics_savesValues() {
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
		metrics.put("OperatingProfitMargin", null);
		metrics.put("UNKNOWN", new BigDecimal("9.99"));
		ReportPredictRequest request = new ReportPredictRequest("20", 20253, metrics);

		// when
		ReportPredictResult result = companyReportMetricPredictService.importPredictedMetrics(request);

		// then
		assertThat(result.savedValues()).isEqualTo(2);
		assertThat(result.skippedMetrics()).isEqualTo(1);
		assertThat(result.reportVersionNo()).isEqualTo(1);
		assertThat(companyReportsRepository.count()).isEqualTo(1);
		assertThat(companyReportVersionsRepository.count()).isEqualTo(1);
		assertThat(companyReportMetricValuesRepository.count()).isEqualTo(2);

		for (CompanyReportMetricValuesEntity value : companyReportMetricValuesRepository.findAll()) {
			assertThat(value.getValueType()).isEqualTo(MetricValueType.PREDICTED);
		}
	}

	@Test
	@DisplayName("기업이 없으면 저장하지 않고 스킵한다")
	void importPredictedMetrics_skipsMissingCompany() {
		// given
		Map<String, BigDecimal> metrics = Map.of("ROA", new BigDecimal("1.23"));
		ReportPredictRequest request = new ReportPredictRequest("999999", 20253, metrics);

		// when
		ReportPredictResult result = companyReportMetricPredictService.importPredictedMetrics(request);

		// then
		assertThat(result.savedValues()).isZero();
		assertThat(result.skippedCompanies()).isEqualTo(1);
		assertThat(companyReportsRepository.count()).isZero();
		assertThat(companyReportMetricValuesRepository.count()).isZero();
	}
}
