# 필터링 문제 원인 분석 및 해결 방법

## 현재 상황
부산광역시를 선택했는데 다른 지역의 매물도 표시되는 문제가 지속됨.

## 가능한 원인

### 1. 데이터 표기 불일치
```sql
-- dongcodes 테이블
sido_name = '부산광역시'

-- 하지만 실제로는:
sido_name = '부산' 
sido_name = '부산광역시 ' (공백 포함)
sido_name = '부산시'
```

### 2. sgg_cd 코드 불일치
```
부산광역시 코드: 26xxx
서울특별시 코드: 11xxx
경기도 코드: 41xxx

만약 dongcodes의 dong_code가 잘못되어 있다면?
예: sido_name은 '부산광역시'인데 dong_code는 '11110...'(서울)
```

### 3. CONCAT 길이 문제
```
sgg_cd = '2611' (4자리) + umd_cd = '010100' (6자리) = '2611010100' (10자리)
                         잘못됨 ❌
```

### 4. NULL 또는 빈 문자열 처리
```
dongNameList 폴백 매칭 시:
- umd_nm IN ('중앙동', '남포동', ...)
- 하지만 서울에도 '중앙동'이 있음
- 튜플 매칭 실패 시 dongName으로 매칭되어 다른 지역도 나옴
```

## 디버깅 방법

### 1. 백엔드 로그 확인
```
========== 필터링 시작 ==========
입력 - sidoName: 부산광역시
생성된 튜플 (처음 10개):
  [0] sgg_cd=26110, umd_cd=10100
  [1] sgg_cd=26110, umd_cd=10200
  ...
========== 필터링 종료 ==========
```

### 2. SQL로 직접 확인
`DEBUG_QUERY.sql` 실행:
```sql
-- 부산광역시의 실제 dong_code 확인
SELECT dong_code, sido_name, gugun_name 
FROM dongcodes 
WHERE sido_name = '부산광역시' 
LIMIT 10;

-- 부산 sgg_cd로 시작하는 매물 확인
SELECT sgg_cd, umd_cd, apt_nm 
FROM houseinfos 
WHERE sgg_cd LIKE '26%' 
LIMIT 10;
```

### 3. 프론트엔드 로그 확인
콘솔에서 표시된 매물의 `aptSeq`, `sggCd`, `umdCd` 확인:
- 부산이 아닌 다른 지역 코드가 있는지 확인

## 임시 해결 방법

### dongNameList 폴백 제거
SQL에서 dong_name 매칭을 완전히 제거하고 오직 튜플 매칭만 사용:
```xml
<if test="sggUmdPairs != null and sggUmdPairs.size() > 0">
  (h.sgg_cd, h.umd_cd) IN (...)
</if>
<!-- dongNameList 조건 제거 -->
```

## 다음 단계
1. 백엔드 재시작 후 로그 확인
2. `DEBUG_QUERY.sql` 실행하여 실제 데이터 확인
3. 문제가 되는 매물의 `apt_seq` 추적


