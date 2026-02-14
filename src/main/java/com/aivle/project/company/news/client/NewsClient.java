package com.aivle.project.company.news.client;

import com.aivle.project.company.news.dto.NewsApiResponse;
import com.aivle.project.company.news.dto.NewsItemResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 서버 뉴스 분석 API 클라이언트.
 */
@Slf4j
@Component
public class NewsClient {

    private final WebClient webClient;
    private final boolean mockEnabled;
    private final long mockLatencyMs;
    private final Duration callTimeout;

    @Autowired
    public NewsClient(
        @Qualifier("aiWebClient") WebClient aiWebClient,
        @Value("${ai.server.mock.enabled:false}") boolean mockEnabled,
        @Value("${ai.server.mock.latency-ms:0}") long mockLatencyMs,
		@Value("${ai.server.http.call-timeout-ms:10000}") long callTimeoutMs
    ) {
        this(aiWebClient, mockEnabled, mockLatencyMs, Duration.ofMillis(callTimeoutMs));
    }

	// 테스트 코드 호환을 위해 URL 기반 생성자를 유지한다.
	NewsClient(String aiServerUrl, boolean mockEnabled, long mockLatencyMs) {
		this(WebClient.builder().baseUrl(aiServerUrl).build(), mockEnabled, mockLatencyMs, Duration.ofMillis(10000L));
	}

	// 타임아웃 테스트를 위한 생성자.
	NewsClient(String aiServerUrl, boolean mockEnabled, long mockLatencyMs, long callTimeoutMs) {
		this(WebClient.builder().baseUrl(aiServerUrl).build(), mockEnabled, mockLatencyMs, Duration.ofMillis(callTimeoutMs));
	}

	private NewsClient(WebClient webClient, boolean mockEnabled, long mockLatencyMs, Duration callTimeout) {
        this.webClient = webClient;
        this.mockEnabled = mockEnabled;
        this.mockLatencyMs = mockLatencyMs;
		this.callTimeout = callTimeout;
    }

    /**
     * AI 서버에서 뉴스 분석 데이터를 가져옵니다.
     *
     * @param companyCode 기업 코드 (stock_code)
     * @param companyName 기업명
     * @return 뉴스 분석 응답
	 */
	@CircuitBreaker(name = "aiServer")
	@Retry(name = "aiServer")
	@Bulkhead(name = "aiServer", type = Bulkhead.Type.SEMAPHORE)
	public NewsApiResponse fetchNews(String companyCode, String companyName) {
		log.info("Requesting news for company: {} ({})", companyCode, companyName);

		if (mockEnabled) {
			applyMockLatency();
			return mockNews(companyCode, companyName);
		}

	        try {
	            return postWithTimeout(
					"/api/v1/news/{companyCode}",
					Map.of("company_name", companyName),
					NewsApiResponse.class,
					companyCode
				);
			} catch (DecodingException e) {
				log.error("Failed to decode AI news response for company {}: {}", companyCode, e.getMessage());
				throw new RuntimeException("AI Server response format error: invalid datetime field", e);
		} catch (Exception e) {
			log.error("Failed to fetch news for company {}: {}", companyCode, e.getMessage());
			throw new RuntimeException("AI Server connection failed", e);
		}
	}

	/**
	 * AI 서버에서 사업보고서 분석 데이터를 가져옵니다.
	 *
	 * @param companyCode 기업 코드 (stock_code)
	 * @return 사업보고서 분석 응답
	 */
	@CircuitBreaker(name = "aiServer")
	@Retry(name = "aiServer")
	@Bulkhead(name = "aiServer", type = Bulkhead.Type.SEMAPHORE)
	public com.aivle.project.company.reportanalysis.dto.ReportApiResponse fetchReport(String companyCode) {
		log.info("Requesting report analysis for company: {}", companyCode);

		if (mockEnabled) {
			applyMockLatency();
			return mockReport(companyCode);
		}

			try {
				return getWithTimeout(
					"/api/v1/news/{companyCode}/report",
					com.aivle.project.company.reportanalysis.dto.ReportApiResponse.class,
					companyCode
				);
			} catch (DecodingException e) {
				log.error("Failed to decode AI report response for company {}: {}", companyCode, e.getMessage());
				throw new RuntimeException("AI Server response format error: invalid datetime field", e);
		} catch (Exception e) {
			log.error("Failed to fetch report analysis for company {}: {}", companyCode, e.getMessage());
			throw new RuntimeException("AI Server connection failed", e);
		}
	}

	private void applyMockLatency() {
		if (mockLatencyMs <= 0) {
			return;
		}
		try {
			Thread.sleep(mockLatencyMs);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private <T> T postWithTimeout(String uriTemplate, Object body, Class<T> responseType, Object... uriVariables) {
		return webClient.post()
			.uri(uriTemplate, uriVariables)
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.retrieve()
			.bodyToMono(responseType)
			.timeout(callTimeout)
			.block();
	}

	private <T> T getWithTimeout(String uriTemplate, Class<T> responseType, Object... uriVariables) {
		return webClient.get()
			.uri(uriTemplate, uriVariables)
			.retrieve()
			.bodyToMono(responseType)
			.timeout(callTimeout)
			.block();
	}

	private NewsApiResponse mockNews(String companyCode, String companyName) {
		LocalDateTime now = LocalDateTime.now();
		List<NewsItemResponse> items = List.of(
			new NewsItemResponse("모의 뉴스 1", "요약 1", 0.71, now.minusMinutes(2).toString(), "https://example.com/news/1", "POS"),
			new NewsItemResponse("모의 뉴스 2", "요약 2", 0.22, now.minusMinutes(5).toString(), "https://example.com/news/2", "NEU"),
			new NewsItemResponse("모의 뉴스 3", "요약 3", -0.12, now.minusMinutes(8).toString(), "https://example.com/news/3", "NEG")
		);
		return new NewsApiResponse(
			companyName == null ? "PERF_MOCK_COMPANY" : companyName,
			items.size(),
			items,
			0.27,
			now.toString()
		);
	}

	private com.aivle.project.company.reportanalysis.dto.ReportApiResponse mockReport(String companyCode) {
		LocalDateTime now = LocalDateTime.now();
		List<NewsItemResponse> items = List.of(
			new NewsItemResponse("모의 리포트 1", "리포트 요약 1", 0.61, now.minusMinutes(3).toString(), "https://example.com/report/1", "POS"),
			new NewsItemResponse("모의 리포트 2", "리포트 요약 2", 0.14, now.minusMinutes(7).toString(), "https://example.com/report/2", "NEU")
		);
		return new com.aivle.project.company.reportanalysis.dto.ReportApiResponse(
			"PERF_MOCK_COMPANY_" + companyCode,
			items.size(),
			items,
			0.38,
			now.toString()
		);
	}
}
