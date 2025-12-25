-- 잘못된 좌표를 가진 아파트 확인

-- 1. 특정 동 코드의 아파트 정보 확인
SELECT 
    h.apt_seq,
    h.sgg_cd,
    h.umd_cd,
    h.apt_nm,
    h.umd_nm,
    h.jibun,
    h.latitude,
    h.longitude,
    CASE 
        WHEN h.latitude BETWEEN 35.0 AND 35.5 AND h.longitude BETWEEN 128.9 AND 129.3 THEN '부산 좌표'
        WHEN h.latitude BETWEEN 37.4 AND 37.7 AND h.longitude BETWEEN 126.8 AND 127.2 THEN '서울 좌표'
        ELSE '기타 지역'
    END as location_check
FROM houseinfos h
WHERE h.sgg_cd = '26260'  -- 부산 동래구
  AND h.umd_cd = '10800'
LIMIT 20;

-- 2. 부산 동 코드인데 서울 좌표를 가진 경우 찾기
SELECT 
    COUNT(*) as wrong_count,
    h.sgg_cd,
    h.umd_cd,
    h.umd_nm
FROM houseinfos h
WHERE h.sgg_cd LIKE '26%'  -- 부산광역시
  AND h.latitude BETWEEN 37.4 AND 37.7  -- 서울 위도
  AND h.longitude BETWEEN 126.8 AND 127.2  -- 서울 경도
GROUP BY h.sgg_cd, h.umd_cd, h.umd_nm
ORDER BY wrong_count DESC;

-- 3. 반대로 서울 동 코드인데 부산 좌표를 가진 경우
SELECT 
    COUNT(*) as wrong_count,
    h.sgg_cd,
    h.umd_cd,
    h.umd_nm
FROM houseinfos h
WHERE h.sgg_cd LIKE '11%'  -- 서울특별시
  AND h.latitude BETWEEN 35.0 AND 35.5  -- 부산 위도
  AND h.longitude BETWEEN 128.9 AND 129.3  -- 부산 경도
GROUP BY h.sgg_cd, h.umd_cd, h.umd_nm
ORDER BY wrong_count DESC;



