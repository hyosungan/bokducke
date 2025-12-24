-- 디버깅: 부산광역시 검색 시 실제 데이터 확인

-- 1. dongcodes에서 부산광역시 데이터 확인
SELECT 
    dong_code,
    sido_name,
    gugun_name,
    dong_name,
    LEFT(dong_code, 5) as sgg_cd,
    RIGHT(dong_code, 5) as umd_cd
FROM dongcodes
WHERE sido_name = '부산광역시'
LIMIT 20;

-- 2. houseinfos에서 부산 sgg_cd 확인 (부산은 26으로 시작)
SELECT DISTINCT
    h.sgg_cd,
    h.umd_cd,
    h.umd_nm,
    COUNT(*) as count
FROM houseinfos h
WHERE h.sgg_cd LIKE '26%'
GROUP BY h.sgg_cd, h.umd_cd, h.umd_nm
LIMIT 20;

-- 3. sido_name 다양한 표기 확인
SELECT DISTINCT sido_name, COUNT(*) as count
FROM dongcodes
WHERE sido_name LIKE '%부산%'
GROUP BY sido_name;

-- 4. 실제 매칭 가능 여부 확인
SELECT 
    dc.dong_code,
    dc.sido_name,
    dc.gugun_name,
    dc.dong_name,
    h.sgg_cd,
    h.umd_cd,
    h.umd_nm,
    h.apt_nm
FROM dongcodes dc
INNER JOIN houseinfos h 
    ON LEFT(dc.dong_code, 5) = h.sgg_cd 
    AND RIGHT(dc.dong_code, 5) = h.umd_cd
WHERE dc.sido_name = '부산광역시'
LIMIT 20;

-- 5. sgg_cd가 26이 아닌데 매칭되는 경우 확인 (문제 케이스)
SELECT 
    h.sgg_cd,
    h.umd_cd,
    h.umd_nm,
    h.apt_nm,
    COUNT(*) as count
FROM houseinfos h
WHERE h.sgg_cd NOT LIKE '26%'
  AND (h.sgg_cd, h.umd_cd) IN (
    SELECT LEFT(dong_code, 5), RIGHT(dong_code, 5)
    FROM dongcodes
    WHERE sido_name = '부산광역시'
  )
GROUP BY h.sgg_cd, h.umd_cd, h.umd_nm, h.apt_nm
LIMIT 20;


