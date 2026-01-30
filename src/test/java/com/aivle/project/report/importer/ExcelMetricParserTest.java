package com.aivle.project.report.importer;

import static org.assertj.core.api.Assertions.assertThat;

import com.aivle.project.report.dto.CompanyMetricValueCommand;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExcelMetricParserTest {

	private final ExcelMetricParser parser = new ExcelMetricParser();

	@Test
	@DisplayName("엑셀 파일에서 지표 명령을 파싱한다")
	void parseExcel() throws Exception {
		// given
		Path filePath = Path.of("input_demo.xlsx");
		try (InputStream inputStream = Files.newInputStream(filePath)) {
			// when
			List<CompanyMetricValueCommand> commands = parser.parse(inputStream);

			// then
			assertThat(commands).isNotEmpty();
			assertThat(commands).anyMatch(command ->
				"20".equals(command.stockCode())
					&& "ROA".equals(command.metricCode())
					&& command.quarterOffset() == 0
					&& command.metricValue() != null
					&& command.rowIndex() > 1
					&& command.colIndex() > 1
			);
			assertThat(commands).anyMatch(command ->
				"20".equals(command.stockCode())
					&& "OpMargin".equals(command.metricCode())
					&& command.quarterOffset() == -3
					&& "매출액영업이익률_분기-3".equals(command.headerName())
			);
		}
	}
}
