package com.aivle.project.company.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.aivle.project.company.config.DartProperties;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.web.reactive.function.client.WebClient;

class DartCorpCodeItemReaderTest {

	private MockWebServer mockWebServer;

	@BeforeEach
	void setUp() throws Exception {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterEach
	void tearDown() throws Exception {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("ZIP/XML 응답을 파싱해 기업 목록을 읽는다")
	void shouldReadItemsFromZipXml() throws Exception {
		// given
		String xml = """
			<?xml version=\"1.0\" encoding=\"UTF-8\"?>
			<result>
			  <status>000</status>
			  <message>OK</message>
			  <list>
			    <corp_code>001</corp_code>
			    <corp_name>회사1</corp_name>
			    <corp_eng_name>Company1</corp_eng_name>
			    <stock_code>123456</stock_code>
			    <modify_date>20250101</modify_date>
			  </list>
			  <list>
			    <corp_code>002</corp_code>
			    <corp_name>회사2</corp_name>
			    <corp_eng_name></corp_eng_name>
			    <stock_code></stock_code>
			    <modify_date>20241231</modify_date>
			  </list>
			</result>
			""";
		byte[] zipBytes = createZipBytes("corpCode.xml", xml);
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/zip")
			.setBody(new Buffer().write(zipBytes)));

		DartProperties properties = new DartProperties();
		properties.setApiKey("test-key");
		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();
		DartCorpCodeItemReader reader = new DartCorpCodeItemReader(properties, webClient);

		// when
		reader.open(new ExecutionContext());
		DartCorpCodeItem first = reader.read();
		DartCorpCodeItem second = reader.read();
		DartCorpCodeItem end = reader.read();
		reader.close();

		// then
		assertThat(first.corpCode()).isEqualTo("001");
		assertThat(first.corpName()).isEqualTo("회사1");
		assertThat(first.modifyDate()).isEqualTo(LocalDate.of(2025, 1, 1));

		assertThat(second.corpCode()).isEqualTo("002");
		assertThat(second.corpEngName()).isEqualTo("");
		assertThat(second.stockCode()).isEqualTo("");
		assertThat(second.modifyDate()).isEqualTo(LocalDate.of(2024, 12, 31));

		assertThat(end).isNull();
	}

	private byte[] createZipBytes(String entryName, String xml) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
			ZipEntry entry = new ZipEntry(entryName);
			zipOutputStream.putNextEntry(entry);
			zipOutputStream.write(xml.getBytes(StandardCharsets.UTF_8));
			zipOutputStream.closeEntry();
		}
		return outputStream.toByteArray();
	}
}
