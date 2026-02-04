package com.aivle.project.company.entity;

import com.aivle.project.common.entity.BaseEntity;
import com.aivle.project.industry.entity.IndustryEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * companies 테이블에 매핑되는 기업 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "companies")
public class CompaniesEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "corp_code", nullable = false, length = 8, columnDefinition = "CHAR(8)")
	private String corpCode;

	@Column(name = "corp_name", nullable = false, length = 100)
	private String corpName;

	@Column(name = "corp_eng_name", length = 200)
	private String corpEngName;

	@Column(name = "stock_code", length = 6, columnDefinition = "CHAR(6)")
	private String stockCode;

	@Column(name = "modify_date")
	private LocalDate modifyDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "industry_code")
	private IndustryEntity industryName;

	/**
	 * 기업 엔티티 생성.
	 */
	public static CompaniesEntity create(
		String corpCode,
		String corpName,
		String corpEngName,
		String stockCode,
		LocalDate modifyDate
	) {
		CompaniesEntity company = new CompaniesEntity();
		company.corpCode = corpCode;
		company.corpName = corpName;
		company.corpEngName = corpEngName;
		company.stockCode = stockCode;
		company.modifyDate = modifyDate;
		return company;
	}
}
