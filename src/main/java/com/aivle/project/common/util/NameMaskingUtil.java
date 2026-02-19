package com.aivle.project.common.util;

/**
 * 사용자 이름 마스킹 유틸리티.
 */
public final class NameMaskingUtil {

	private NameMaskingUtil() {
	}

	public static String mask(String name) {
		if (name == null) {
			return null;
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty()) {
			return trimmed;
		}
		int[] codePoints = trimmed.codePoints().toArray();
		if (codePoints.length == 1) {
			return "*";
		}
		if (codePoints.length == 2) {
			return new StringBuilder()
				.appendCodePoint(codePoints[0])
				.append("*")
				.toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.appendCodePoint(codePoints[0]);
		for (int i = 1; i < codePoints.length - 1; i++) {
			sb.append("*");
		}
		sb.appendCodePoint(codePoints[codePoints.length - 1]);
		return sb.toString();
	}
}
