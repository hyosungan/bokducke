-- dong_code 슬라이싱 논리 검증 쿼리

-- 1. dong_code 길이 분포 확인
SELECT 
    LENGTH(dong_code) as code_length,
    COUNT(*) as count
FROM dongcodes
GROUP BY LENGTH(dong_code)
ORDER BY code_length;

-- 2. houseinfos의 sgg_cd, umd_cd 길이 분포 확인
SELECT 
    LENGTH(h.sgg_cd) as sgg_cd_length,
    LENGTH(h.umd_cd) as umd_cd_length,
    COUNT(*) as count
FROM houseinfos h
WHERE h.sgg_cd IS NOT NULL AND h.umd_cd IS NOT NULL
GROUP BY LENGTH(h.sgg_cd), LENGTH(h.umd_cd)
ORDER BY sgg_cd_length, umd_cd_length;

-- 3. dong_code를 슬라이싱했을 때 houseinfos와 매칭되는지 확인 (샘플)
SELECT 
    dc.dong_code,
    LEFT(dc.dong_code, 5) as sliced_sgg_cd,
    RIGHT(dc.dong_code, 5) as sliced_umd_cd,
    h.sgg_cd as actual_sgg_cd,
    h.umd_cd as actual_umd_cd,
    CASE 
        WHEN LEFT(dc.dong_code, 5) = h.sgg_cd AND RIGHT(dc.dong_code, 5) = h.umd_cd 
        THEN 'MATCH' 
        ELSE 'MISMATCH' 
    END as match_status
FROM dongcodes dc
INNER JOIN houseinfos h ON CONCAT(h.sgg_cd, h.umd_cd) = dc.dong_code
WHERE h.sgg_cd IS NOT NULL AND h.umd_cd IS NOT NULL
LIMIT 100;

-- 4. 불일치하는 케이스 확인 (있다면)
SELECT 
    dc.dong_code,
    LEFT(dc.dong_code, 5) as sliced_sgg_cd,
    RIGHT(dc.dong_code, 5) as sliced_umd_cd,
    h.sgg_cd as actual_sgg_cd,
    h.umd_cd as actual_umd_cd
FROM dongcodes dc
INNER JOIN houseinfos h ON CONCAT(h.sgg_cd, h.umd_cd) = dc.dong_code
WHERE h.sgg_cd IS NOT NULL 
  AND h.umd_cd IS NOT NULL
  AND (LEFT(dc.dong_code, 5) != h.sgg_cd OR RIGHT(dc.dong_code, 5) != h.umd_cd)
LIMIT 100;



