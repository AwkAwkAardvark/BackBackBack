package com.aivle.project.report.repository;

import com.aivle.project.report.entity.CompanyReportsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회사 보고서 조회/저장 리포지토리.
 */
public interface CompanyReportsRepository extends JpaRepository<CompanyReportsEntity, Long> {

	Optional<CompanyReportsEntity> findByCompanyIdAndQuarterId(Long companyId, Long quarterId);
}
