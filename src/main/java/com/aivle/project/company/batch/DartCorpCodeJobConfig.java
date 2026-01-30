package com.aivle.project.company.batch;

import com.aivle.project.company.config.DartProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.JobRepository;

/**
 * DART 기업 목록 동기화 배치 설정.
 */
@Configuration
@RequiredArgsConstructor
public class DartCorpCodeJobConfig {

	public static final String JOB_NAME = "dartCorpCodeSyncJob";
	public static final String STEP_NAME = "dartCorpCodeSyncStep";

	private final DartProperties dartProperties;

	@Bean
	public Job dartCorpCodeSyncJob(JobRepository jobRepository, Step dartCorpCodeSyncStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
			.start(dartCorpCodeSyncStep)
			.build();
	}

	@Bean
	public Step dartCorpCodeSyncStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		ItemReader<DartCorpCodeItem> reader,
		ItemProcessor<DartCorpCodeItem, DartCorpCodeItem> processor,
		ItemWriter<DartCorpCodeItem> writer
	) {
		int chunkSize = dartProperties.getCorpSync().getChunkSize();
		return new StepBuilder(STEP_NAME, jobRepository)
			.<DartCorpCodeItem, DartCorpCodeItem>chunk(chunkSize, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();
	}
}
