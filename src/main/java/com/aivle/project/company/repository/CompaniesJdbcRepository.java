package com.aivle.project.company.repository;

import com.aivle.project.company.batch.DartCorpCodeItem;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 기업 목록 동기화를 위한 JDBC 저장소.
 */
@Repository
@RequiredArgsConstructor
public class CompaniesJdbcRepository {

	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public Map<String, LocalDate> findModifyDatesByCorpCodes(List<String> corpCodes) {
		if (corpCodes == null || corpCodes.isEmpty()) {
			return Collections.emptyMap();
		}
		MapSqlParameterSource params = new MapSqlParameterSource("codes", corpCodes);
		List<CorpModifyDateRow> rows = namedParameterJdbcTemplate.query(
			"SELECT corp_code, modify_date FROM companies WHERE corp_code IN (:codes)",
			params,
			(rs, rowNum) -> new CorpModifyDateRow(
				rs.getString("corp_code"),
				toLocalDate(rs.getDate("modify_date"))
			)
		);
		Map<String, LocalDate> result = new HashMap<>();
		for (CorpModifyDateRow row : rows) {
			result.put(row.corpCode(), row.modifyDate());
		}
		return result;
	}

	public void upsertBatch(List<DartCorpCodeItem> items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		String sql = """
			INSERT INTO companies (corp_code, corp_name, corp_eng_name, stock_code, modify_date)
			VALUES (?, ?, ?, ?, ?)
			ON DUPLICATE KEY UPDATE
				corp_name = VALUES(corp_name),
				corp_eng_name = VALUES(corp_eng_name),
				stock_code = VALUES(stock_code),
				modify_date = VALUES(modify_date)
			""";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
				DartCorpCodeItem item = items.get(i);
				ps.setString(1, item.corpCode());
				ps.setString(2, item.corpName());
				ps.setString(3, item.corpEngName());
				ps.setString(4, item.stockCode());
				ps.setDate(5, toSqlDate(item.modifyDate()));
			}

			@Override
			public int getBatchSize() {
				return items.size();
			}
		});
	}

	private static LocalDate toLocalDate(Date date) {
		return date == null ? null : date.toLocalDate();
	}

	private static Date toSqlDate(LocalDate date) {
		return date == null ? null : Date.valueOf(date);
	}

	private record CorpModifyDateRow(String corpCode, LocalDate modifyDate) {
	}
}
