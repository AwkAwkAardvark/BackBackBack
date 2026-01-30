package com.aivle.project.company.service;

import com.aivle.project.company.dto.CompanySearchResponse;
import com.aivle.project.company.entity.CompaniesEntity;
import com.aivle.project.company.repository.CompaniesRepository;
import java.util.List;
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

	public List<CompanySearchResponse> search(String keyword) {
		String normalized = normalizeKeyword(keyword);
		if (normalized.length() < MIN_KEYWORD_LENGTH) {
			throw new IllegalArgumentException("keyword는 2자 이상이어야 합니다.");
		}

		List<CompaniesEntity> companies = companiesRepository
			.findTop20ByCorpNameContainingIgnoreCaseOrCorpEngNameContainingIgnoreCaseOrderByCorpNameAsc(
				normalized,
				normalized
			);
		log.info("기업 검색 완료: keyword={}, count={}", normalized, companies.size());
		return companies.stream()
			.map(company -> new CompanySearchResponse(
				company.getId(),
				company.getCorpName(),
				company.getCorpEngName(),
				company.getStockCode()
			))
			.toList();
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}
		return keyword.trim();
	}
}
