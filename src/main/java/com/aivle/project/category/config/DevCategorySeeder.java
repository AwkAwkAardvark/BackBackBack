package com.aivle.project.category.config;

import com.aivle.project.category.entity.CategoriesEntity;
import com.aivle.project.category.repository.CategoriesRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * dev 환경 카테고리 기본 데이터 시딩.
 */
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevCategorySeeder implements ApplicationRunner {

	private final CategoriesRepository categoriesRepository;

	@Override
	public void run(ApplicationArguments args) {
		if (categoriesRepository.count() > 0) {
			return;
		}

		List<CategoriesEntity> categories = List.of(
			CategoriesEntity.create("자유게시판", "자유롭게 이야기해요", 1, true, null),
			CategoriesEntity.create("공지사항", "서비스 공지와 업데이트", 2, true, null),
			CategoriesEntity.create("Q&A", "질문과 답변을 공유해요", 3, true, null),
			CategoriesEntity.create("후기", "사용 경험을 나눠요", 4, true, null)
		);
		categoriesRepository.saveAll(categories);
	}
}
