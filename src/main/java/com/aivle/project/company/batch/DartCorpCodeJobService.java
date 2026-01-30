package com.aivle.project.company.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.stereotype.Service;

/**
 * DART 기업 목록 동기화 배치 실행 서비스.
 */
@Service
@RequiredArgsConstructor
public class DartCorpCodeJobService {

	private final JobLauncher jobLauncher;
	private final org.springframework.batch.core.Job dartCorpCodeSyncJob;

	public JobExecution launch(String trigger)
		throws JobExecutionAlreadyRunningException, JobRestartException,
		JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters parameters = new JobParametersBuilder()
			.addLong("requestedAt", System.currentTimeMillis())
			.addString("trigger", trigger)
			.toJobParameters();
		return jobLauncher.run(dartCorpCodeSyncJob, parameters);
	}
}
