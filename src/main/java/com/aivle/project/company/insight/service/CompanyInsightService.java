package com.aivle.project.company.insight.service;

import com.aivle.project.company.entity.CompaniesEntity;
import com.aivle.project.company.insight.dto.CompanyInsightDto;
import com.aivle.project.company.insight.dto.CompanyInsightType;
import com.aivle.project.common.error.ExternalAiUnavailableException;
import com.aivle.project.company.news.entity.NewsAnalysisEntity;
import com.aivle.project.company.news.entity.NewsArticleEntity;
import com.aivle.project.company.news.repository.NewsAnalysisRepository;
import com.aivle.project.company.news.repository.NewsArticleRepository;
import com.aivle.project.company.news.service.NewsService;
import com.aivle.project.company.reportanalysis.entity.ReportAnalysisEntity;
import com.aivle.project.company.reportanalysis.entity.ReportContentEntity;
import com.aivle.project.company.reportanalysis.repository.ReportAnalysisRepository;
import com.aivle.project.company.reportanalysis.repository.ReportContentRepository;
import com.aivle.project.company.reportanalysis.service.ReportAnalysisService;
import com.aivle.project.company.repository.CompaniesRepository;
import com.aivle.project.company.service.CompanyReputationScoreService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * 기업 인사이트 조회 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyInsightService {

	private final CompaniesRepository companiesRepository;
	private final NewsAnalysisRepository newsAnalysisRepository;
	private final NewsArticleRepository newsArticleRepository;
	private final ReportAnalysisRepository reportAnalysisRepository;
	private final ReportContentRepository reportContentRepository;
	private final NewsService newsService;
	private final ReportAnalysisService reportAnalysisService;
	private final CompanyReputationScoreService companyReputationScoreService;

	public InsightResult getInsights(
		Long companyId,
		int newsPage,
		int newsSize,
		int reportPage,
		int reportSize,
		boolean refresh
	) {
		CompaniesEntity company = companiesRepository.findById(companyId)
			.orElseThrow(() -> new IllegalArgumentException("Company not found for id: " + companyId));

		String stockCode = company.getStockCode();
		if (stockCode == null || stockCode.isBlank()) {
			throw new IllegalArgumentException("Company has no stockCode: " + companyId);
		}
		String companyName = company.getCorpName();

		Optional<ReportAnalysisEntity> latestReportOpt;
		Optional<NewsAnalysisEntity> latestNewsOpt;
		ExternalAiUnavailableException externalFailure = null;

		if (refresh) {
			// 인사이트 강제 갱신 시 뉴스/보고서를 모두 재수집한다.
			try {
				newsService.refreshLatestNews(stockCode);
			} catch (ExternalAiUnavailableException ex) {
				externalFailure = ex;
				log.warn("인사이트 강제 갱신 중 뉴스 수집 실패: companyId={}, stockCode={}, reasonCode={}",
					companyId, stockCode, ex.getReasonCode());
			}
			try {
				reportAnalysisService.fetchAndStoreReport(stockCode);
			} catch (ExternalAiUnavailableException ex) {
				if (externalFailure == null) {
					externalFailure = ex;
				}
				log.warn("인사이트 강제 갱신 중 보고서 수집 실패: companyId={}, stockCode={}, reasonCode={}",
					companyId, stockCode, ex.getReasonCode());
			}
		}

		latestReportOpt = reportAnalysisRepository
			.findTopByCompanyIdOrderByAnalyzedAtDesc(companyId);
		latestNewsOpt = newsAnalysisRepository
			.findTopByCompanyIdOrderByAnalyzedAtDesc(companyId);

		boolean hasReport = latestReportOpt.isPresent()
			&& reportContentRepository.existsByReportAnalysisId(latestReportOpt.get().getId());
		boolean hasNews = latestNewsOpt.isPresent()
			&& newsArticleRepository.existsByNewsAnalysisId(latestNewsOpt.get().getId());

		ReportAnalysisEntity latestReport = latestReportOpt.orElse(null);
		NewsAnalysisEntity latestNews = latestNewsOpt.orElse(null);

		if (!hasReport) {
			java.util.Optional<ReportContentEntity> existingReportContent =
				reportContentRepository.findTopByReportAnalysisCompanyIdOrderByPublishedAtDesc(companyId);
			if (existingReportContent.isPresent()) {
				latestReport = existingReportContent.get().getReportAnalysis();
				hasReport = true;
			}
		}

		if (!hasNews) {
			java.util.Optional<NewsArticleEntity> existingNewsArticle =
				newsArticleRepository.findTopByNewsAnalysisCompanyIdOrderByPublishedAtDesc(companyId);
			if (existingNewsArticle.isPresent()) {
				latestNews = existingNewsArticle.get().getNewsAnalysis();
				hasNews = true;
			}
		}

			if (!hasReport) {
				try {
					reportAnalysisService.fetchAndStoreReport(stockCode);
				} catch (ExternalAiUnavailableException ex) {
					if (externalFailure == null) {
						externalFailure = ex;
					}
					log.warn("인사이트 조회 중 보고서 수집 실패: companyId={}, stockCode={}, reasonCode={}",
						companyId, stockCode, ex.getReasonCode());
				}
				latestReport = reportAnalysisRepository
					.findTopByCompanyIdOrderByAnalyzedAtDesc(companyId)
					.orElse(null);
			}
			if (!hasNews) {
				try {
					newsService.refreshLatestNews(stockCode);
				} catch (ExternalAiUnavailableException ex) {
					if (externalFailure == null) {
						externalFailure = ex;
					}
					log.warn("인사이트 조회 중 뉴스 수집 실패: companyId={}, stockCode={}, reasonCode={}",
						companyId, stockCode, ex.getReasonCode());
				}
				latestNews = newsAnalysisRepository
					.findTopByCompanyIdOrderByAnalyzedAtDesc(companyId)
					.orElse(null);
			}
			try {
				companyReputationScoreService.syncExternalHealthScoreIfPresent(companyId, stockCode);
			} catch (ExternalAiUnavailableException ex) {
				if (externalFailure == null) {
					externalFailure = ex;
				}
				log.warn("인사이트 조회 중 평판 점수 동기화 실패: companyId={}, stockCode={}, reasonCode={}",
					companyId, stockCode, ex.getReasonCode());
			}

		List<CompanyInsightDto> items = new java.util.ArrayList<>();

		if (latestReport != null) {
			Page<ReportContentEntity> reportContents = reportContentRepository
				.findByReportAnalysisIdOrderByPublishedAtDesc(latestReport.getId(), PageRequest.of(reportPage, reportSize));
			for (ReportContentEntity content : reportContents.getContent()) {
				items.add(CompanyInsightDto.builder()
					.id(content.getId())
					.type(CompanyInsightType.REPORT)
					.title(content.getTitle())
					.body(content.getSummary())
					.content(null)
					.source(null)
					.publishedAt(resolvePublishedAt(content.getPublishedAt(), latestReport.getAnalyzedAt()))
					.url(content.getLink())
					.build());
			}
		}

			if (latestNews != null) {
			Page<NewsArticleEntity> newsArticles = newsArticleRepository
				.findByNewsAnalysisIdOrderByPublishedAtDesc(latestNews.getId(), PageRequest.of(newsPage, newsSize));
			for (NewsArticleEntity article : newsArticles.getContent()) {
				items.add(CompanyInsightDto.builder()
					.id(article.getId())
					.type(CompanyInsightType.NEWS)
					.title(article.getTitle())
					.body(null)
					.content(article.getSummary())
					.source(article.getSentiment())
					.publishedAt(resolvePublishedAt(article.getPublishedAt(), latestNews.getAnalyzedAt()))
					.url(article.getLink())
					.build());
			}
			}

			if (items.isEmpty() && externalFailure != null) {
				throw externalFailure;
			}
			return new InsightResult(items, latestNews != null ? latestNews.getAverageScore() : null, false);
		}

	private LocalDateTime resolvePublishedAt(LocalDateTime publishedAt, LocalDateTime fallback) {
		return publishedAt != null ? publishedAt : fallback;
	}

	public record InsightResult(List<CompanyInsightDto> items, java.math.BigDecimal averageScore, boolean processing) {
	}
}
