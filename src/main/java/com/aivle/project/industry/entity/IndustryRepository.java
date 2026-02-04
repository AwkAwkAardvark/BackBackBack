package com.aivle.project.industry.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndustryRepository extends JpaRepository<IndustryEntity, Long> {

    Optional<IndustryEntity> findByIndustryCode(String industryCode);
}
