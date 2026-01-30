package com.aivle.project.company.repository;

import com.aivle.project.company.entity.CompaniesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 기업 조회/저장 리포지토리.
 */
public interface CompaniesRepository extends JpaRepository<CompaniesEntity, Long> {

	Optional<CompaniesEntity> findByStockCode(String stockCode);

	List<CompaniesEntity> findTop20ByCorpNameContainingIgnoreCaseOrCorpEngNameContainingIgnoreCaseOrderByCorpNameAsc(
		String corpName,
		String corpEngName
	);
}
