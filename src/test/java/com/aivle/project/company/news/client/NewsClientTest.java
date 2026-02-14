package com.aivle.project.company.news.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aivle.project.company.news.dto.NewsApiResponse;
import com.aivle.project.company.reportanalysis.dto.ReportApiResponse;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NewsClientTest {

	@Test
	@DisplayName("mock 모드에서 뉴스 수집 응답을 반환한다")
	void fetchNews_shouldReturnMockResponseWhenMockModeEnabled() {
		// given
		NewsClient newsClient = new NewsClient("http://localhost:8080", true, 0);

		// when
		NewsApiResponse response = newsClient.fetchNews("900001", "PERF_MOCK_COMPANY");

		// then
		assertThat(response).isNotNull();
		assertThat(response.companyName()).isEqualTo("PERF_MOCK_COMPANY");
		assertThat(response.news()).isNotEmpty();
	}

	@Test
	@DisplayName("mock 모드에서 사업보고서 수집 응답을 반환한다")
	void fetchReport_shouldReturnMockResponseWhenMockModeEnabled() {
		// given
		NewsClient newsClient = new NewsClient("http://localhost:8080", true, 0);

		// when
		ReportApiResponse response = newsClient.fetchReport("900001");

		// then
		assertThat(response).isNotNull();
		assertThat(response.news()).isNotEmpty();
		assertThat(response.averageScore()).isNotNull();
	}

	@Test
	@DisplayName("실제 호출 경로에서 call-timeout 초과 시 예외가 발생한다")
	void fetchNews_shouldThrowWhenTimeoutExceeded() throws Exception {
		// given
		try (MockWebServer server = new MockWebServer()) {
			server.start();
			NewsClient newsClient = new NewsClient(server.url("/").toString(), false, 0, 100);
			server.enqueue(new MockResponse()
				.setBody("""
					{
					  "company_name": "테스트",
					  "total_count": 0,
					  "news": [],
					  "average_score": 0.0,
					  "analyzed_at": "2026-02-14T12:00:00"
					}
					""")
				.addHeader("Content-Type", "application/json")
				.setBodyDelay(500, TimeUnit.MILLISECONDS));

			// when & then
			assertThatThrownBy(() -> newsClient.fetchNews("900001", "테스트"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("AI Server connection failed");
		}
	}
}
