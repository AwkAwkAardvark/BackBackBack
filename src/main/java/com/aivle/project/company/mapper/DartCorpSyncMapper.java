package com.aivle.project.company.mapper;

import com.aivle.project.company.dto.DartCorpSyncResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.batch.core.JobExecution;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DartCorpSyncMapper {

	// 배치 실행 결과를 응답 DTO로 변환한다.
	@Mapping(target = "jobExecutionId", source = "id")
	@Mapping(target = "status", expression = "java(execution.getStatus().name())")
	DartCorpSyncResponse toResponse(JobExecution execution);
}
