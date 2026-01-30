package com.aivle.project.company.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DART API 및 기업 목록 동기화 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "dart")
public class DartProperties {

	/**
	 * DART API 인증키 (환경변수 DART_API_KEY).
	 */
	private String apiKey;

	private final CorpSync corpSync = new CorpSync();
	private final Api api = new Api();
	private final Http http = new Http();

	@Getter
	@Setter
	public static class CorpSync {

		/**
		 * 배치 처리 청크 크기.
		 */
		private int chunkSize = 1000;

		private final Schedule schedule = new Schedule();
	}

	@Getter
	@Setter
	public static class Schedule {

		/**
		 * 스케줄러 활성화 여부 (기본 OFF).
		 */
		private boolean enabled = false;

		/**
		 * 크론 표현식 (기본: 매일 03:00 KST).
		 */
		private String cron = "0 0 3 * * *";
	}

	@Getter
	@Setter
	public static class Http {

		/**
		 * 연결 타임아웃 (ms).
		 */
		private int connectTimeoutMs = 3000;

		/**
		 * 응답 타임아웃 (ms).
		 */
		private int responseTimeoutMs = 10000;

		/**
		 * 재시도 횟수.
		 */
		private int retryCount = 2;

		/**
		 * 재시도 대기 시간 (ms).
		 */
		private int retryBackoffMs = 1000;
	}

	@Getter
	@Setter
	public static class Api {

		/**
		 * 응답 버퍼 최대 크기.
		 */
		private org.springframework.util.unit.DataSize maxBufferSize =
			org.springframework.util.unit.DataSize.ofMegabytes(50);
	}
}
