# 보고서 지표 엑셀 업로드

관리자 API로 엑셀 지표를 업로드하면 기업/분기/버전/지표 값이 자동으로 저장됩니다.

## 엔드포인트
- `POST /admin/reports/metrics/import`
- Content-Type: `multipart/form-data`
- 권한: `ROLE_ADMIN`

## 요청 파라미터
| 파라미터 | 설명 | 필수 |
| --- | --- | --- |
| quarterKey | 기준 분기 키(YYYYQ, 예: 20253) | Y |
| file | 엑셀 파일(.xlsx) | Y |

## 엑셀 헤더 규칙
- 첫 행에 `기업코드` 컬럼이 있어야 합니다.
- 지표는 `{지표명}_{현재/분기-1/분기-2/분기-3}` 형식입니다.
- 지원 지표명은 `MetricHeaderMapping` 기준입니다.

## 저장 흐름 요약
1. `기업코드`를 정규화(숫자만 추출, 6자리 0 패딩)하여 `companies.stock_code`로 매핑합니다.
2. 기준 분기(quarterKey)와 분기 오프셋(0, -1, -2, -3)을 계산해 `quarters`를 조회/생성합니다.
3. `company_reports`(company_id, quarter_id) 생성/조회 → `company_report_versions` 버전 증가 → `company_report_metric_values` 저장.
4. 빈 값은 `metric_value`에 `NULL`로 저장합니다.
5. 기업/지표 미존재는 스킵하며 로그에 남깁니다.

## 예시 (cURL)
```bash
curl -X POST "http://localhost:8080/admin/reports/metrics/import" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -F "quarterKey=20253" \
  -F "file=@input_demo.xlsx"
```
