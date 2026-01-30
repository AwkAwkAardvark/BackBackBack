package com.aivle.project.report.repository;

import com.aivle.project.report.dto.ReportMetricRowProjection;
import com.aivle.project.report.entity.CompanyReportMetricValuesEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 보고서 지표 값 조회/저장 리포지토리.
 */
public interface CompanyReportMetricValuesRepository extends JpaRepository<CompanyReportMetricValuesEntity, Long> {

	@Query("""
		select c.corpName as corpName,
			c.stockCode as stockCode,
			m.metricCode as metricCode,
			m.metricNameKo as metricNameKo,
			v.metricValue as metricValue,
			v.valueType as valueType,
			q.quarterKey as quarterKey,
			rv.versionNo as versionNo,
			rv.generatedAt as generatedAt
		from CompanyReportMetricValuesEntity v
		join v.reportVersion rv
		join rv.companyReport cr
		join cr.company c
		join v.metric m
		join v.quarter q
		where c.stockCode = :stockCode
			and q.quarterKey between :fromQuarterKey and :toQuarterKey
			and rv.versionNo = (
				select max(rv2.versionNo)
				from CompanyReportVersionsEntity rv2
				where rv2.companyReport = cr
			)
		order by q.quarterKey, m.metricCode
		""")
	List<ReportMetricRowProjection> findLatestMetricsByStockCodeAndQuarterRange(
		@Param("stockCode") String stockCode,
		@Param("fromQuarterKey") int fromQuarterKey,
		@Param("toQuarterKey") int toQuarterKey
	);
}
