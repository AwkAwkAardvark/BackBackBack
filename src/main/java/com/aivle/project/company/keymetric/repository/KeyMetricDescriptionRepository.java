package com.aivle.project.company.keymetric.repository;

import com.aivle.project.company.keymetric.entity.KeyMetricDescriptionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 핵심 건강도 설명 리포지토리.
 */
public interface KeyMetricDescriptionRepository extends JpaRepository<KeyMetricDescriptionEntity, Long> {

	Optional<KeyMetricDescriptionEntity> findByMetricCode(String metricCode);

	List<KeyMetricDescriptionEntity> findAllByMetricCodeIn(List<String> metricCodes);
}
