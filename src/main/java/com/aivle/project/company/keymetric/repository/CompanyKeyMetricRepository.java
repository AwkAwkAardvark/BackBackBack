package com.aivle.project.company.keymetric.repository;

import com.aivle.project.company.keymetric.entity.CompanyKeyMetricEntity;
import com.aivle.project.company.keymetric.entity.CompanyKeyMetricRiskLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

/**
 * 기업 핵심 건강도 리포지토리.
 */
public interface CompanyKeyMetricRepository extends JpaRepository<CompanyKeyMetricEntity, Long> {

	Optional<CompanyKeyMetricEntity> findByCompanyIdAndQuarterId(Long companyId, Long quarterId);

	Optional<CompanyKeyMetricEntity> findByCompanyIdAndQuarter_QuarterKey(Long companyId, int quarterKey);

	@EntityGraph(attributePaths = {"company", "quarter"})
	List<CompanyKeyMetricEntity> findByCompanyIdIn(List<Long> companyIds);

	@EntityGraph(attributePaths = {"company", "quarter"})
	List<CompanyKeyMetricEntity> findByCompanyIdInAndQuarter_QuarterKey(List<Long> companyIds, int quarterKey);

	@EntityGraph(attributePaths = {"company", "quarter"})
	@Query("""
		select ckm
		from CompanyKeyMetricEntity ckm
		join ckm.quarter q
		where ckm.company.id in :companyIds
			and q.quarterKey <= :latestActualQuarterKey
			and ckm.riskLevel is not null
			and ckm.deletedAt is null
		order by q.quarterKey desc, ckm.company.id asc
		""")
	List<CompanyKeyMetricEntity> findRiskRecordsByCompanyIds(
		@Param("companyIds") List<Long> companyIds,
		@Param("latestActualQuarterKey") int latestActualQuarterKey,
		Pageable pageable
	);

	@EntityGraph(attributePaths = {"company", "quarter"})
	@Query("""
		select ckm
		from CompanyKeyMetricEntity ckm
		join ckm.quarter q
		where ckm.company.id in :companyIds
			and q.quarterKey in :quarterKeys
			and ckm.deletedAt is null
		""")
	List<CompanyKeyMetricEntity> findByCompanyIdInAndQuarterKeyIn(
		@Param("companyIds") List<Long> companyIds,
		@Param("quarterKeys") List<Integer> quarterKeys
	);

	@Query("""
		select ckm.company.id as companyId, q.quarterKey as quarterKey, ckm.riskLevel as riskLevel
		from CompanyKeyMetricEntity ckm
		join ckm.quarter q
		where ckm.company.id in :companyIds
			and q.quarterKey <= :latestActualQuarterKey
			and ckm.riskLevel is not null
			and ckm.deletedAt is null
		""")
	List<CompanyRiskHistoryProjection> findRiskHistoryByCompanyIds(
		@Param("companyIds") List<Long> companyIds,
		@Param("latestActualQuarterKey") int latestActualQuarterKey
	);

	interface CompanyRiskHistoryProjection {
		Long getCompanyId();
		Integer getQuarterKey();
		CompanyKeyMetricRiskLevel getRiskLevel();
	}
}
