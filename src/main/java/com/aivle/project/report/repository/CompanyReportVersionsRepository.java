package com.aivle.project.report.repository;

import com.aivle.project.report.entity.CompanyReportVersionsEntity;
import com.aivle.project.report.entity.CompanyReportsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 보고서 버전 조회/저장 리포지토리.
 */
public interface CompanyReportVersionsRepository extends JpaRepository<CompanyReportVersionsEntity, Long> {

	Optional<CompanyReportVersionsEntity> findTopByCompanyReportOrderByVersionNoDesc(CompanyReportsEntity companyReport);
}
