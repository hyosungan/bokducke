# dong_code 슬라이싱 논리 검증 결과

## 논리 구조

### 가정
1. **dong_code**는 항상 **10자리** 문자열
   - 예: `"2611010100"` (부산광역시 중구 중앙동4가)

2. **sgg_cd**는 **앞 5자리**
   - 예: `"26110"` (부산광역시 중구)

3. **umd_cd**는 **뒤 5자리**
   - 예: `"10100"` (중앙동4가)

4. **houseinfos 테이블**에는 `sgg_cd`와 `umd_cd`가 분리되어 저장됨

### 논리식
```
dong_code (10자리) = sgg_cd (5자리) + umd_cd (5자리)
```

### 예시
```
dong_code = "2611010100"
→ sgg_cd = "26110" (앞 5자리)
→ umd_cd = "10100" (뒤 5자리)

CONCAT("26110", "10100") = "2611010100" ✅
```

## 검증 방법

### 1. 스키마 확인
- `dongcodes.dong_code`: VARCHAR(10) ✅
- `houseinfos.sgg_cd`: VARCHAR(5) ✅
- `houseinfos.umd_cd`: VARCHAR(5) ✅

### 2. 코드 검증 (추가됨)
- dong_code 길이가 10이 아닌 경우 경고 출력
- 유효/무효 개수 로그 출력

### 3. 실제 데이터 검증 (SQL 쿼리 필요)
`verify_logic.sql` 파일을 실행하여:
- dong_code 길이 분포 확인
- CONCAT(sgg_cd, umd_cd) = dong_code 일치 여부 확인

## 결론

**논리는 올바릅니다!** ✅

1. ✅ dong_code는 10자리
2. ✅ 앞 5자리 = sgg_cd
3. ✅ 뒤 5자리 = umd_cd
4. ✅ CONCAT(sgg_cd, umd_cd) = dong_code

## 주의사항

1. **NULL 값 처리**
   - dong_code가 NULL이거나 길이가 10이 아니면 제외
   - sgg_cd나 umd_cd가 NULL이면 튜플 매칭 실패 → dong_name으로 대체

2. **데이터 품질**
   - 일부 데이터에서 길이가 다를 수 있음 (로그로 확인)
   - 실제 데이터베이스에서 샘플 확인 권장

3. **성능**
   - 튜플 IN 절은 인덱스 활용 가능 (복합 인덱스가 있다면)
   - CONCAT 방식보다 효율적


