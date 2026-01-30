package com.aivle.project.company.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DartCorpCodeItemProcessorTest {

	@Test
	@DisplayName("corp_code가 비어있으면 null을 반환한다")
	void shouldReturnNullWhenCorpCodeMissing() throws Exception {
		// given
		DartCorpCodeItemProcessor processor = new DartCorpCodeItemProcessor();
		DartCorpCodeItem item = new DartCorpCodeItem(" ", "회사", null, null, LocalDate.of(2025, 1, 1));

		// when
		DartCorpCodeItem result = processor.process(item);

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("필드값을 trim 처리한다")
	void shouldNormalizeFields() throws Exception {
		// given
		DartCorpCodeItemProcessor processor = new DartCorpCodeItemProcessor();
		DartCorpCodeItem item = new DartCorpCodeItem(" 001 ", " 회사 ", " Eng ", " 123456 ",
			LocalDate.of(2025, 1, 1));

		// when
		DartCorpCodeItem result = processor.process(item);

		// then
		assertThat(result.corpCode()).isEqualTo("001");
		assertThat(result.corpName()).isEqualTo("회사");
		assertThat(result.corpEngName()).isEqualTo("Eng");
		assertThat(result.stockCode()).isEqualTo("123456");
	}
}
