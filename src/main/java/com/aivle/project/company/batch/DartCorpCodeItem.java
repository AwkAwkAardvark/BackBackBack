package com.aivle.project.company.batch;

import java.time.LocalDate;

/**
 * DART 기업 코드 단건 정보.
 */
public record DartCorpCodeItem(
	String corpCode,
	String corpName,
	String corpEngName,
	String stockCode,
	LocalDate modifyDate
) {
}
