package com.aivle.project.company.service;

import com.aivle.project.company.dto.CompanyConfirmRequest;
import com.aivle.project.company.dto.CompanyConfirmResult;
import com.aivle.project.company.dto.CompanyOverviewResponse;
import com.aivle.project.company.dto.CompanySectorResponse;
import com.aivle.project.company.dto.CompanySummaryResponse;
import com.aivle.project.company.dto.ModelStatus;
import com.aivle.project.company.dto.RiskLevel;
import com.aivle.project.company.dto.UpdateRequestCreateRequest;
import com.aivle.project.company.dto.UpdateRequestCreateResponse;
import com.aivle.project.company.entity.CompaniesEntity;
import com.aivle.project.company.repository.CompaniesRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 기업 조회 서비스.
 */
@Service
@RequiredArgsConstructor
public class CompanyQueryService {

	private static final CompanySectorResponse DEFAULT_SECTOR = new CompanySectorResponse("UNKNOWN", "미분류");
	private static final RiskLevel DEFAULT_RISK = RiskLevel.SAFE;
	private static final ModelStatus DEFAULT_MODEL_STATUS = ModelStatus.EXISTING;

	private final CompaniesRepository companiesRepository;

	public List<CompanySummaryResponse> listCompanies(
		String query,
		Integer page,
		Integer size
	) {
		PageRequest pageRequest = PageRequest.of(
			Math.max(0, Objects.requireNonNullElse(page, 0)),
			resolveSize(size),
			Sort.by(Sort.Direction.ASC, "corpName")
		);
		Page<CompaniesEntity> companies = searchCompanies(query, pageRequest);
		return companies.stream()
			.map(this::toSummaryResponse)
			.toList();
	}

	public CompanySummaryResponse getCompanySummary(String companyId) {
		CompaniesEntity company = findCompanyById(companyId)
			.orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));
		return toSummaryResponse(company);
	}

	public CompanyOverviewResponse getCompanyOverview(String companyId) {
		CompanySummaryResponse summary = getCompanySummary(companyId);
		return new CompanyOverviewResponse(
			summary,
			null,
			List.of(),
			List.of(),
			null,
			null,
			DEFAULT_MODEL_STATUS
		);
	}

	public CompanyConfirmResult confirmCompany(CompanyConfirmRequest request) {
		CompaniesEntity company = resolveCompanyForConfirm(request)
			.orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));
		return new CompanyConfirmResult(
			String.valueOf(company.getId()),
			company.getCorpName(),
			DEFAULT_SECTOR,
			DEFAULT_MODEL_STATUS,
			true,
			resolveLastAnalyzedAt(company.getModifyDate())
		);
	}

	public UpdateRequestCreateResponse createUpdateRequest(String companyId, UpdateRequestCreateRequest request) {
		findCompanyById(companyId)
			.orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));
		long id = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		return new UpdateRequestCreateResponse(id);
	}

	private Optional<CompaniesEntity> resolveCompanyForConfirm(CompanyConfirmRequest request) {
		if (request == null) {
			return Optional.empty();
		}
		Optional<CompaniesEntity> byId = findCompanyById(request.companyId());
		if (byId.isPresent()) {
			return byId;
		}
		if (request.code() != null && !request.code().isBlank()) {
			Optional<CompaniesEntity> byStock = companiesRepository.findByStockCode(request.code().trim());
			if (byStock.isPresent()) {
				return byStock;
			}
		}
		if (request.name() != null && !request.name().isBlank()) {
			return companiesRepository
				.findTop20ByCorpNameContainingIgnoreCaseOrCorpEngNameContainingIgnoreCaseOrderByCorpNameAsc(
					request.name().trim(),
					request.name().trim()
				)
				.stream()
				.findFirst();
		}
		return Optional.empty();
	}

	private Optional<CompaniesEntity> findCompanyById(String companyId) {
		if (companyId == null || companyId.isBlank()) {
			return Optional.empty();
		}
		try {
			return companiesRepository.findById(Long.valueOf(companyId.trim()));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	private CompanySummaryResponse toSummaryResponse(CompaniesEntity entity) {
		return new CompanySummaryResponse(
			String.valueOf(entity.getId()),
			entity.getCorpName(),
			DEFAULT_SECTOR,
			0.0,
			DEFAULT_RISK,
			resolveLastUpdatedAt(entity.getModifyDate()),
			null
		);
	}

	private Page<CompaniesEntity> searchCompanies(String query, PageRequest pageRequest) {
		if (query == null || query.isBlank()) {
			return companiesRepository.findAll(pageRequest);
		}
		String normalized = query.trim();
		return companiesRepository.findByCorpNameContainingIgnoreCaseOrCorpEngNameContainingIgnoreCase(
			normalized,
			normalized,
			pageRequest
		);
	}

	private int resolveSize(Integer size) {
		if (size == null || size <= 0) {
			return 20;
		}
		return Math.min(size, 100);
	}

	private String resolveLastUpdatedAt(LocalDate modifyDate) {
		if (modifyDate == null) {
			return null;
		}
		return modifyDate.atStartOfDay().atOffset(ZoneOffset.UTC).toString();
	}

	private String resolveLastAnalyzedAt(LocalDate modifyDate) {
		if (modifyDate != null) {
			return modifyDate.atStartOfDay().atOffset(ZoneOffset.UTC).toString();
		}
		return OffsetDateTime.now(ZoneOffset.UTC).toString();
	}
}
