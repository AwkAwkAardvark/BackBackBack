package com.aivle.project.company.mapper;

import com.aivle.project.company.dto.CompanySearchItemResponse;
import com.aivle.project.company.entity.CompaniesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

	// 검색 응답은 내부 id를 companyId로 노출한다.
	@Mapping(target = "companyId", source = "id")
	@Mapping(target = "name", source = "corpName")
	@Mapping(target = "code", source = "stockCode")
	@Mapping(target = "sector", ignore = true)
	CompanySearchItemResponse toSearchItemResponse(CompaniesEntity company);
}
