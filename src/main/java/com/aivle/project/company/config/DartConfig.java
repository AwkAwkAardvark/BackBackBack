package com.aivle.project.company.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * DART 관련 설정 바인딩.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(DartProperties.class)
public class DartConfig {

	@Bean
	public WebClient dartWebClient(DartProperties dartProperties) {
		DartProperties.Http http = dartProperties.getHttp();
		DartProperties.Api api = dartProperties.getApi();
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, http.getConnectTimeoutMs())
			.responseTimeout(Duration.ofMillis(http.getResponseTimeoutMs()));
		int maxBufferBytes = Math.toIntExact(api.getMaxBufferSize().toBytes());
		ExchangeStrategies strategies = ExchangeStrategies.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxBufferBytes))
			.build();
		return WebClient.builder()
			.baseUrl("https://opendart.fss.or.kr")
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(strategies)
			.build();
	}
}
