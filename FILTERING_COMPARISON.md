# 필터링 방식 비교 분석

## 현재 방식 (CONCAT 방식)

### 동작:
1. "서울특별시" 검색
2. dongcodes에서 sido_name='서울특별시'인 모든 dong_code 조회
   - 예: ["1111010100", "1111010200", "1111020100", ...]
3. SQL에서: `CONCAT(h.sgg_cd, h.umd_cd) IN ('1111010100', '1111010200', ...)`

### 문제점:
- CONCAT 연산이 모든 row에 대해 실행됨 (인덱스 활용 어려움)
- dong_code_list가 매우 클 수 있음 (서울은 수천 개)
- IN 절의 리스트가 너무 크면 성능 저하

## 제안 방식 (슬라이싱 방식)

### 동작:
1. "서울특별시" 검색
2. dongcodes에서 sido_name='서울특별시'인 모든 dong_code 조회
3. 각 dong_code를 슬라이싱:
   - "1111010100" → sgg_cd="11110", umd_cd="10100"
   - "1111010200" → sgg_cd="11110", umd_cd="10200"
   - ...
4. 고유한 sgg_cd_list와 umd_cd_list 생성
5. SQL에서: `h.sgg_cd IN (sgg_cd_list) AND h.umd_cd IN (umd_cd_list)`

### 장점:
- sgg_cd, umd_cd에 인덱스가 있다면 활용 가능
- 리스트 크기가 줄어듦 (중복 제거)
- 더 직관적이고 명확한 매칭

### 단점:
- 두 개의 IN 절을 AND로 연결하면 Cartesian product 문제 발생 가능
- 예: sgg_cd IN (10개) AND umd_cd IN (100개) = 10*100 = 1000개 조합
- 하지만 실제로는 (sgg_cd, umd_cd) 쌍이 dong_code로 제한되므로 문제 없음

## 개선된 방식 (튜플 방식 - 가장 정확)

### 동작:
1. "서울특별시" 검색
2. dongcodes에서 sido_name='서울특별시'인 모든 dong_code 조회
3. 각 dong_code를 (sgg_cd, umd_cd) 튜플로 변환
4. SQL에서: `(h.sgg_cd, h.umd_cd) IN (('11110', '10100'), ('11110', '10200'), ...)`

### 장점:
- 가장 정확한 매칭 (조합을 정확히 지정)
- 인덱스 활용 가능 (복합 인덱스가 있다면)
- Cartesian product 문제 없음

### 단점:
- 튜플 리스트가 클 수 있음 (하지만 dong_code_list와 동일한 크기)

## 권장 사항

**튜플 방식**을 추천합니다:
- 정확도: ✅ 가장 정확
- 성능: ✅ 인덱스 활용 가능
- 명확성: ✅ 직관적


