# 보고서 지표 분기별 조회

기업의 보고서 지표를 최신 버전 기준으로 분기별 그룹핑하여 조회합니다.

## 엔드포인트
- `GET /admin/reports/metrics/grouped`
- 권한: `ROLE_ADMIN`

## 요청 파라미터
| 파라미터 | 설명 | 필수 |
| --- | --- | --- |
| stockCode | 기업코드(stock_code) | Y |
| fromQuarterKey | 조회 시작 분기키(YYYYQ) | Y |
| toQuarterKey | 조회 종료 분기키(YYYYQ) | Y |

## 응답 구조(요약)
```
{
  "success": true,
  "data": {
    "corpName": "...",
    "stockCode": "...",
    "fromQuarterKey": 20244,
    "toQuarterKey": 20253,
    "quarters": [
      {
        "quarterKey": 20244,
        "versionNo": 2,
        "generatedAt": "2026-01-30T12:00:00",
        "metrics": [
          { "metricCode": "ROA", "metricNameKo": "...", "metricValue": 1.23, "valueType": "ACTUAL" }
        ]
      }
    ]
  }
}
```

## 예시 (cURL)
```bash
curl -X GET "http://localhost:8080/admin/reports/metrics/grouped?stockCode=000020&fromQuarterKey=20244&toQuarterKey=20253" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```
