-- 방법 2: 데이터베이스에서 잘못된 좌표 수정 (근본적 해결)

-- 1. 잘못된 좌표 개수 확인
SELECT 
    '부산 코드 + 서울 좌표' as issue_type,
    COUNT(*) as count
FROM houseinfos
WHERE sgg_cd LIKE '26%'
  AND latitude BETWEEN 37.4 AND 37.7
  AND longitude BETWEEN 126.8 AND 127.2

UNION ALL

SELECT 
    '서울 코드 + 부산 좌표' as issue_type,
    COUNT(*) as count
FROM houseinfos
WHERE sgg_cd LIKE '11%'
  AND latitude BETWEEN 35.0 AND 35.5
  AND longitude BETWEEN 128.9 AND 129.3;

-- 2. 잘못된 좌표를 NULL로 설정 (지도에 표시 안 되게)
UPDATE houseinfos
SET latitude = NULL, longitude = NULL
WHERE (
    -- 부산 코드인데 서울 좌표
    (sgg_cd LIKE '26%' 
     AND latitude BETWEEN 37.4 AND 37.7 
     AND longitude BETWEEN 126.8 AND 127.2)
    OR
    -- 서울 코드인데 부산 좌표
    (sgg_cd LIKE '11%' 
     AND latitude BETWEEN 35.0 AND 35.5 
     AND longitude BETWEEN 128.9 AND 129.3)
);

-- 실행 전 확인:
-- SELECT COUNT(*) FROM houseinfos WHERE latitude IS NULL;


