package com.aivle.project.common.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * 외부 AI 서버 전용 WebClient 설정.
 */
@Configuration
public class AiWebClientConfig {

	@Bean(name = "aiWebClient")
	public WebClient aiWebClient(
		@Value("${ai.server.url}") String aiServerUrl,
		@Value("${ai.server.http.connect-timeout-ms:3000}") int connectTimeoutMs,
		@Value("${ai.server.http.response-timeout-ms:8000}") int responseTimeoutMs,
		@Value("${ai.server.http.max-connections:100}") int maxConnections,
		@Value("${ai.server.http.pending-acquire-timeout-ms:2000}") long pendingAcquireTimeoutMs
	) {
		ConnectionProvider provider = ConnectionProvider.builder("ai-client-pool")
			.maxConnections(maxConnections)
			.pendingAcquireTimeout(Duration.ofMillis(pendingAcquireTimeoutMs))
			.build();

		HttpClient httpClient = HttpClient.create(provider)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
			.responseTimeout(Duration.ofMillis(responseTimeoutMs))
			.followRedirect(true)
			.compress(true);

		return WebClient.builder()
			.baseUrl(aiServerUrl)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}
}
