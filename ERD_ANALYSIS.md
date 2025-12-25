# dongcodes와 houseinfos 테이블 연관 관계 분석

## 테이블 구조

### dongcodes (법정동코드 테이블)
- `dong_code` (VARCHAR(10), PK): 법정동코드 (예: "2611010100")
- `sido_name` (VARCHAR(30)): 시도이름 (예: "부산광역시")
- `gugun_name` (VARCHAR(30)): 구군이름 (예: "중구")
- `dong_name` (VARCHAR(30)): 동이름 (예: "중앙동4가")

### houseinfos (주택정보 테이블)
- `sgg_cd` (VARCHAR(5)): 시군구코드 (예: "26110")
- `umd_cd` (VARCHAR(5)): 읍면동코드 (예: "10100")
- `umd_nm` (VARCHAR(20)): 읍면동이름 (예: "중앙동4가")

## 연관 관계

### 방법 1: dong_code로 매칭 (가장 정확)
```
CONCAT(houseinfos.sgg_cd, houseinfos.umd_cd) = dongcodes.dong_code
```
- 예: CONCAT("26110", "10100") = "2611010100"
- ✅ **가장 정확한 방법**
- 문제: sgg_cd나 umd_cd가 NULL이면 매칭 실패

### 방법 2: dong_name으로 매칭 (덜 정확)
```
houseinfos.umd_nm = dongcodes.dong_name
```
- 예: "중앙동4가" = "중앙동4가"
- ⚠️ **문제**: 같은 동 이름이 다른 지역에도 존재할 수 있음
- 예: "중앙동"이 서울, 부산, 대전 등 여러 지역에 존재

## 현재 코드의 문제점

현재 SQL은 두 방법을 OR로 연결:
```sql
(CONCAT(sgg_cd, umd_cd) IN dongCodeList) 
OR 
(umd_nm IN dongNameList)
```

이렇게 하면:
1. dong_code로 매칭 실패 시
2. dong_name으로 매칭하는데, 다른 지역의 같은 동 이름과도 매칭됨
3. 결과: 부산광역시를 검색했는데 서울의 "중앙동"도 나올 수 있음

## 해결 방법

dong_name 매칭은 **같은 sido_name/gugun_name 내에서만** 수행해야 함.
하지만 현재는 dongNameList에 이미 필터링된 목록만 들어있으므로 이론적으로는 문제없어야 함.

하지만 실제 데이터를 확인해봐야 할 것 같습니다.



