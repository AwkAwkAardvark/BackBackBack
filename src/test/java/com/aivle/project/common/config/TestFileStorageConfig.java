package com.aivle.project.common.config;

import com.aivle.project.file.storage.FileStorageService;
import com.aivle.project.file.storage.StoredFile;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 테스트 프로파일 전용 파일 스토리지 스텁 설정.
 */
@Configuration
@Profile("test")
public class TestFileStorageConfig {

	private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

	@Bean
	@Primary
	public FileStorageService fileStorageService() {
		return (MultipartFile file, String keyPrefix) -> {
			int index = SEQUENCE.getAndIncrement();
			String storedKey = buildKey(keyPrefix, index, file.getOriginalFilename());
			return new StoredFile(
				"memory://" + storedKey,
				file.getOriginalFilename(),
				file.getSize(),
				file.getContentType(),
				storedKey
			);
		};
	}

	private String buildKey(String keyPrefix, int index, String filename) {
		String safeName = filename == null ? "unknown" : filename;
		String prefix = (keyPrefix == null || keyPrefix.isBlank()) ? "test" : keyPrefix;
		return prefix + "/" + index + "-" + safeName;
	}
}
