package com.aivle.project.company.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * DART 기업 목록 동기화 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dart.corp-sync.schedule", name = "enabled", havingValue = "true")
public class DartCorpCodeScheduler {

	private final DartCorpCodeJobService jobService;

	@Scheduled(cron = "${dart.corp-sync.schedule.cron:0 0 3 * * *}")
	public void runScheduledJob() {
		try {
			JobExecution execution = jobService.launch("schedule");
			log.info("DART 기업 목록 동기화 배치 실행 완료. jobExecutionId={}, status={}",
				execution.getId(), execution.getStatus());
		} catch (Exception ex) {
			log.warn("DART 기업 목록 동기화 배치 실행 실패", ex);
		}
	}
}
