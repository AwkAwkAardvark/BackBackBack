package com.aivle.project.company.service;

import com.aivle.project.company.dto.CompanySearchItemResponse;
import com.aivle.project.company.entity.CompaniesEntity;
import com.aivle.project.company.repository.CompaniesRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 기업 검색 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanySearchService {

	private static final int MIN_KEYWORD_LENGTH = 2;

	private final CompaniesRepository companiesRepository;
	private final com.aivle.project.company.mapper.CompanyMapper companyMapper;

	public List<CompanySearchItemResponse> searchByName(String name, Integer limit) {
		String normalized = normalizeKeyword(name);
		if (normalized.length() < MIN_KEYWORD_LENGTH) {
			throw new IllegalArgumentException("name은 2자 이상이어야 합니다.");
		}

		List<CompaniesEntity> companies = companiesRepository
			.findTop20ByCorpNameContainingIgnoreCaseOrCorpEngNameContainingIgnoreCaseOrderByCorpNameAsc(
				normalized,
				normalized
			);
		if (companies.isEmpty()) {
			return Collections.emptyList();
		}

		int capped = applyLimit(companies.size(), limit);
		List<CompanySearchItemResponse> response = companies.stream()
			.limit(capped)
			.map(companyMapper::toSearchItemResponse)
			.toList();
		log.info("기업 검색 완료: name={}, count={}", normalized, response.size());
		return response;
	}

	public Optional<CompanySearchItemResponse> searchByCode(String code) {
		String normalized = normalizeKeyword(code);
		if (normalized.isBlank()) {
			return Optional.empty();
		}
		return companiesRepository.findByStockCode(normalized)
			.map(companyMapper::toSearchItemResponse);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}
		return keyword.trim();
	}

	private int applyLimit(int size, Integer limit) {
		if (limit == null || limit <= 0) {
			return size;
		}
		return Math.min(size, limit);
	}
}
