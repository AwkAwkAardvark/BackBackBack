# 기업 검색 API

기업명/영문명으로 `stock_code`를 조회합니다.

## 엔드포인트
- `GET /admin/companies/search`
- 권한: `ROLE_ADMIN`

## 요청 파라미터
| 파라미터 | 설명 | 필수 |
| --- | --- | --- |
| keyword | 검색 키워드(2자 이상) | Y |

## 응답 예시
```
{
  "success": true,
  "data": [
    {
      "companyId": 1,
      "corpName": "테스트기업",
      "corpEngName": "TEST_CO",
      "stockCode": "000020"
    }
  ]
}
```

## 예시 (cURL)
```bash
curl -X GET "http://localhost:8080/admin/companies/search?keyword=테스트" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```
