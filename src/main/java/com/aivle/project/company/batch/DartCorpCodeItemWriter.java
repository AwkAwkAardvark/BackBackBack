package com.aivle.project.company.batch;

import com.aivle.project.company.repository.CompaniesJdbcRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * DART 기업 코드 Writer.
 */
@Component
@RequiredArgsConstructor
public class DartCorpCodeItemWriter implements ItemWriter<DartCorpCodeItem> {

	private final CompaniesJdbcRepository companiesJdbcRepository;

	@Override
	public void write(Chunk<? extends DartCorpCodeItem> items) {
		List<DartCorpCodeItem> candidates = new ArrayList<>();
		Set<String> corpCodes = new HashSet<>();
		for (DartCorpCodeItem item : items) {
			if (item == null || item.corpCode() == null) {
				continue;
			}
			candidates.add(item);
			corpCodes.add(item.corpCode());
		}
		if (candidates.isEmpty()) {
			return;
		}
		Map<String, LocalDate> existingDates = companiesJdbcRepository.findModifyDatesByCorpCodes(List.copyOf(corpCodes));
		List<DartCorpCodeItem> toUpsert = new ArrayList<>();
		for (DartCorpCodeItem item : candidates) {
			LocalDate existingDate = existingDates.get(item.corpCode());
			LocalDate incomingDate = item.modifyDate();
			if (existingDate == null) {
				toUpsert.add(item);
				continue;
			}
			if (incomingDate != null && incomingDate.isAfter(existingDate)) {
				toUpsert.add(item);
			}
		}
		companiesJdbcRepository.upsertBatch(toUpsert);
	}
}
