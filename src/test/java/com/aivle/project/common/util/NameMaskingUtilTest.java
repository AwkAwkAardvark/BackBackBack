package com.aivle.project.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NameMaskingUtilTest {

	@Test
	@DisplayName("이름 길이에 따라 마스킹 규칙을 적용한다")
	void mask_shouldApplyLengthBasedRule() {
		// given
		String oneChar = "가";
		String twoChars = "가나";
		String threeChars = "홍길동";
		String asciiName = "test-user";

		// when & then
		assertThat(NameMaskingUtil.mask(oneChar)).isEqualTo("*");
		assertThat(NameMaskingUtil.mask(twoChars)).isEqualTo("가*");
		assertThat(NameMaskingUtil.mask(threeChars)).isEqualTo("홍*동");
		assertThat(NameMaskingUtil.mask(asciiName)).isEqualTo("t*******r");
	}

	@Test
	@DisplayName("null 또는 공백 이름은 안전하게 처리한다")
	void mask_shouldHandleNullAndBlank() {
		// given
		String nullName = null;
		String blankName = " ";

		// when & then
		assertThat(NameMaskingUtil.mask(nullName)).isNull();
		assertThat(NameMaskingUtil.mask(blankName)).isEmpty();
	}
}
