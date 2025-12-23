package ssafy.bokduck.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.bokduck.dto.HouseDealDto;
import ssafy.bokduck.dto.HouseInfoDto;
import ssafy.bokduck.dto.DongCodeDto;
import ssafy.bokduck.dto.PropertyScrapDto;
import ssafy.bokduck.mapper.HouseMapper;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HouseService {

    private final HouseMapper houseMapper;

    public HouseService(HouseMapper houseMapper) {
        this.houseMapper = houseMapper;
    }

    public List<HouseInfoDto> searchHouseInfos(Map<String, Object> params) {
        // limit 파라미터 검증 및 정규화 (SQL 인젝션 방지)
        if (params.containsKey("limit")) {
            Object limitObj = params.get("limit");
            int limit;
            if (limitObj instanceof Number) {
                limit = ((Number) limitObj).intValue();
            } else {
                try {
                    limit = Integer.parseInt(limitObj.toString());
                } catch (NumberFormatException e) {
                    limit = 100; // 기본값
                }
            }
            // limit 값 검증 (1~1000 사이로 제한)
            if (limit < 1) limit = 1;
            if (limit > 1000) limit = 1000;
            params.put("limit", limit);
        }
        
        // 시/도 또는 시/군/구 이름으로 검색하는 경우, 해당 지역의 dong_code 목록을 가져와서 필터링
        String sidoName = (String) params.get("sidoName");
        String gugunName = (String) params.get("gugunName");
        String umdNm = (String) params.get("umdNm");
        
        if ((sidoName != null && !sidoName.isEmpty()) || (gugunName != null && !gugunName.isEmpty())) {
            // 해당 지역의 dong_code 목록 조회
            System.out.println("=== 지역 필터링 시작 ===");
            System.out.println("sidoName: " + sidoName);
            System.out.println("gugunName: " + gugunName);
            System.out.println("umdNm: " + umdNm);
            
            List<DongCodeDto> dongCodes;
            if (gugunName != null && !gugunName.isEmpty() && sidoName != null && !sidoName.isEmpty()) {
                dongCodes = houseMapper.getDongsBySidoAndGugun(sidoName, gugunName);
                System.out.println("시/도 + 시/군/구로 조회: " + (dongCodes != null ? dongCodes.size() : 0) + "개");
            } else if (gugunName != null && !gugunName.isEmpty()) {
                dongCodes = houseMapper.getDongsByGugun(gugunName);
                System.out.println("시/군/구로 조회: " + (dongCodes != null ? dongCodes.size() : 0) + "개");
            } else {
                // sidoName만 있는 경우 - 모든 dongcodes에서 sidoName으로 필터링
                dongCodes = houseMapper.getDongCodesBySido(sidoName);
                System.out.println("시/도로 조회: " + (dongCodes != null ? dongCodes.size() : 0) + "개");
            }
            
            // umdNm이 있으면 추가 필터링
            if (umdNm != null && !umdNm.isEmpty() && dongCodes != null) {
                dongCodes = dongCodes.stream()
                    .filter(dc -> {
                        String dongName = dc.getDongName();
                        if (dongName == null) return false;
                        if (dongName.equals(umdNm)) return true;
                        if (dongName.startsWith(umdNm + " ")) return true;
                        if (dongName.endsWith(" " + umdNm)) return true;
                        // 마지막 단어 매칭
                        String[] parts = dongName.split(" ");
                        if (parts.length > 0 && parts[parts.length - 1].equals(umdNm)) return true;
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (dongCodes != null && !dongCodes.isEmpty()) {
                // dong_code를 슬라이싱해서 (sgg_cd, umd_cd) 튜플 리스트 생성
                List<java.util.Map<String, String>> sggUmdPairs = new java.util.ArrayList<>();
                List<String> dongNameList = new java.util.ArrayList<>();
                
                int validCount = 0;
                int invalidCount = 0;
                for (DongCodeDto dc : dongCodes) {
                    String dongCode = dc.getDongCode();
                    if (dongCode != null && !dongCode.isEmpty()) {
                        // dong_code 길이 검증 및 슬라이싱
                        if (dongCode.length() == 10) {
                            // 정상: 10자리 dong_code를 앞 5자리(sgg_cd), 뒤 5자리(umd_cd)로 슬라이싱
                            // 논리 검증: dong_code = sgg_cd(5자리) + umd_cd(5자리)
                            String sggCd = dongCode.substring(0, 5);
                            String umdCd = dongCode.substring(5, 10);
                            java.util.Map<String, String> pair = new java.util.HashMap<>();
                            pair.put("sggCd", sggCd);
                            pair.put("umdCd", umdCd);
                            sggUmdPairs.add(pair);
                            validCount++;
                        } else {
                            // 비정상: 길이가 10이 아닌 경우 로그 출력
                            System.out.println("경고: dong_code 길이가 10이 아님 - " + dongCode + " (길이: " + dongCode.length() + ")");
                            invalidCount++;
                        }
                    }
                    
                    // dong_name 목록도 추출 (dong_code가 없는 매물을 위해)
                    String dongName = dc.getDongName();
                    if (dongName != null && !dongName.isEmpty() && !dongNameList.contains(dongName)) {
                        dongNameList.add(dongName);
                    }
                }
                System.out.println("슬라이싱 검증: 유효=" + validCount + ", 무효=" + invalidCount);
                
                params.put("sggUmdPairs", sggUmdPairs);
                params.put("dongNameList", dongNameList);
                System.out.println("sggUmdPairs 크기: " + sggUmdPairs.size());
                System.out.println("dongNameList 크기: " + dongNameList.size());
                
                if (!sggUmdPairs.isEmpty()) {
                    System.out.println("생성된 튜플 샘플 (처음 10개):");
                    int displayCount = Math.min(10, sggUmdPairs.size());
                    for (int i = 0; i < displayCount; i++) {
                        java.util.Map<String, String> pair = sggUmdPairs.get(i);
                        System.out.println("  [" + i + "] sgg_cd='" + pair.get("sggCd") + "', umd_cd='" + pair.get("umdCd") + "'");
                    }
                }
                
                if (!dongNameList.isEmpty()) {
                    System.out.println("dongName 샘플 (처음 10개):");
                    int displayCount = Math.min(10, dongNameList.size());
                    for (int i = 0; i < displayCount; i++) {
                        System.out.println("  [" + i + "] '" + dongNameList.get(i) + "'");
                    }
                }
                System.out.println("========== 지역 필터링 종료 ==========");
            } else {
                // 해당 지역에 매칭되는 dong_code가 없으면 불가능한 값을 넣어 아무것도 매칭되지 않도록 함
                System.out.println("========== 지역 필터링 시작 ==========");
                System.out.println("!!! 경고: 해당 지역의 dong_code를 찾을 수 없음. 빈 결과 반환.");
                System.out.println("입력 - sidoName: '" + params.get("sidoName") + "'");
                System.out.println("입력 - gugunName: '" + params.get("gugunName") + "'");
                System.out.println("입력 - umdNm: '" + params.get("umdNm") + "'");
                System.out.println("========== 지역 필터링 종료 ==========");
                
                List<java.util.Map<String, String>> impossiblePairs = new java.util.ArrayList<>();
                java.util.Map<String, String> impossiblePair = new java.util.HashMap<>();
                impossiblePair.put("sggCd", "__IMPOSSIBLE__");
                impossiblePair.put("umdCd", "__IMPOSSIBLE__");
                impossiblePairs.add(impossiblePair);
                params.put("sggUmdPairs", impossiblePairs);
                params.put("dongNameList", java.util.Collections.emptyList());
            }
            System.out.println("=== 지역 필터링 완료 ===");
        }
        
        // 가격대 파라미터 검증
        if (params.containsKey("minPrice")) {
            try {
                Object minPriceObj = params.get("minPrice");
                if (minPriceObj != null) {
                    int minPrice = minPriceObj instanceof Number 
                        ? ((Number) minPriceObj).intValue() 
                        : Integer.parseInt(minPriceObj.toString());
                    if (minPrice < 0) minPrice = 0;
                    params.put("minPrice", minPrice);
                }
            } catch (NumberFormatException e) {
                params.remove("minPrice");
            }
        }
        if (params.containsKey("maxPrice")) {
            try {
                Object maxPriceObj = params.get("maxPrice");
                if (maxPriceObj != null) {
                    int maxPrice = maxPriceObj instanceof Number 
                        ? ((Number) maxPriceObj).intValue() 
                        : Integer.parseInt(maxPriceObj.toString());
                    if (maxPrice < 0) maxPrice = 0;
                    params.put("maxPrice", maxPrice);
                }
            } catch (NumberFormatException e) {
                params.remove("maxPrice");
            }
        }
        
        // 평수 파라미터 검증
        if (params.containsKey("minArea")) {
            try {
                Object minAreaObj = params.get("minArea");
                if (minAreaObj != null) {
                    double minArea = minAreaObj instanceof Number 
                        ? ((Number) minAreaObj).doubleValue() 
                        : Double.parseDouble(minAreaObj.toString());
                    if (minArea < 0) minArea = 0;
                    params.put("minArea", minArea);
                }
            } catch (NumberFormatException e) {
                params.remove("minArea");
            }
        }
        if (params.containsKey("maxArea")) {
            try {
                Object maxAreaObj = params.get("maxArea");
                if (maxAreaObj != null) {
                    double maxArea = maxAreaObj instanceof Number 
                        ? ((Number) maxAreaObj).doubleValue() 
                        : Double.parseDouble(maxAreaObj.toString());
                    if (maxArea < 0) maxArea = 0;
                    params.put("maxArea", maxArea);
                }
            } catch (NumberFormatException e) {
                params.remove("maxArea");
            }
        }
        
        return houseMapper.searchHouseInfos(params);
    }

    public HouseInfoDto getHouseDetail(String aptSeq) {
        return houseMapper.findHouseInfoByAptSeq(aptSeq);
    }

    public List<HouseDealDto> getHouseDeals(String aptSeq) {
        return houseMapper.findDealsByAptSeq(aptSeq);
    }

    public List<DongCodeDto> searchDongCodes(String dongName) {
        return houseMapper.searchDongCodes(dongName);
    }

    public List<String> getSidoNames() {
        return houseMapper.getSidoNames();
    }

    public List<String> getGugunNames(String sidoName) {
        return houseMapper.getGugunNames(sidoName);
    }

    public List<DongCodeDto> getDongsByGugun(String gugunName) {
        return houseMapper.getDongsByGugun(gugunName);
    }
    
    public List<DongCodeDto> getDongsBySidoAndGugun(String sidoName, String gugunName) {
        return houseMapper.getDongsBySidoAndGugun(sidoName, gugunName);
    }

    public int countHouseInfos(Map<String, Object> params) {
        // searchHouseInfos와 동일한 로직으로 dongCodeList와 dongNameList 설정
        String sidoName = (String) params.get("sidoName");
        String gugunName = (String) params.get("gugunName");
        String umdNm = (String) params.get("umdNm");
        
        if ((sidoName != null && !sidoName.isEmpty()) || (gugunName != null && !gugunName.isEmpty())) {
            // 해당 지역의 dong_code 목록 조회
            List<DongCodeDto> dongCodes;
            if (gugunName != null && !gugunName.isEmpty() && sidoName != null && !sidoName.isEmpty()) {
                dongCodes = houseMapper.getDongsBySidoAndGugun(sidoName, gugunName);
            } else if (gugunName != null && !gugunName.isEmpty()) {
                dongCodes = houseMapper.getDongsByGugun(gugunName);
            } else {
                // sidoName만 있는 경우
                dongCodes = houseMapper.getDongCodesBySido(sidoName);
            }
            
            // umdNm이 있으면 추가 필터링
            if (umdNm != null && !umdNm.isEmpty() && dongCodes != null) {
                dongCodes = dongCodes.stream()
                    .filter(dc -> {
                        String dongName = dc.getDongName();
                        if (dongName == null) return false;
                        if (dongName.equals(umdNm)) return true;
                        if (dongName.startsWith(umdNm + " ")) return true;
                        if (dongName.endsWith(" " + umdNm)) return true;
                        // 마지막 단어 매칭
                        String[] parts = dongName.split(" ");
                        if (parts.length > 0 && parts[parts.length - 1].equals(umdNm)) return true;
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (dongCodes != null && !dongCodes.isEmpty()) {
                List<String> dongCodeList = dongCodes.stream()
                    .map(DongCodeDto::getDongCode)
                    .filter(code -> code != null && !code.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
                
                List<String> dongNameList = dongCodes.stream()
                    .map(DongCodeDto::getDongName)
                    .filter(name -> name != null && !name.isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
                
                params.put("dongCodeList", dongCodeList);
                params.put("dongNameList", dongNameList);
            } else {
                params.put("dongCodeList", java.util.Collections.emptyList());
                params.put("dongNameList", java.util.Collections.emptyList());
            }
        }
        
        // 가격대 파라미터 검증
        if (params.containsKey("minPrice")) {
            try {
                Object minPriceObj = params.get("minPrice");
                if (minPriceObj != null) {
                    int minPrice = minPriceObj instanceof Number 
                        ? ((Number) minPriceObj).intValue() 
                        : Integer.parseInt(minPriceObj.toString());
                    if (minPrice < 0) minPrice = 0;
                    params.put("minPrice", minPrice);
                }
            } catch (NumberFormatException e) {
                params.remove("minPrice");
            }
        }
        if (params.containsKey("maxPrice")) {
            try {
                Object maxPriceObj = params.get("maxPrice");
                if (maxPriceObj != null) {
                    int maxPrice = maxPriceObj instanceof Number 
                        ? ((Number) maxPriceObj).intValue() 
                        : Integer.parseInt(maxPriceObj.toString());
                    if (maxPrice < 0) maxPrice = 0;
                    params.put("maxPrice", maxPrice);
                }
            } catch (NumberFormatException e) {
                params.remove("maxPrice");
            }
        }
        
        // 평수 파라미터 검증
        if (params.containsKey("minArea")) {
            try {
                Object minAreaObj = params.get("minArea");
                if (minAreaObj != null) {
                    double minArea = minAreaObj instanceof Number 
                        ? ((Number) minAreaObj).doubleValue() 
                        : Double.parseDouble(minAreaObj.toString());
                    if (minArea < 0) minArea = 0;
                    params.put("minArea", minArea);
                }
            } catch (NumberFormatException e) {
                params.remove("minArea");
            }
        }
        if (params.containsKey("maxArea")) {
            try {
                Object maxAreaObj = params.get("maxArea");
                if (maxAreaObj != null) {
                    double maxArea = maxAreaObj instanceof Number 
                        ? ((Number) maxAreaObj).doubleValue() 
                        : Double.parseDouble(maxAreaObj.toString());
                    if (maxArea < 0) maxArea = 0;
                    params.put("maxArea", maxArea);
                }
            } catch (NumberFormatException e) {
                params.remove("maxArea");
            }
        }
        
        return houseMapper.countHouseInfos(params);
    }

    public List<HouseInfoDto> getScrappedHouses(Long userId) {
        return houseMapper.findScrappedHouses(userId);
    }

    public void toggleScrap(Long userId, String aptSeq) {
        int exists = houseMapper.checkScrap(userId, aptSeq);
        if (exists > 0) {
            houseMapper.deleteScrap(userId, aptSeq);
        } else {
            PropertyScrapDto scrap = new PropertyScrapDto();
            scrap.setUserId(userId);
            scrap.setAptSeq(aptSeq);
            houseMapper.insertScrap(scrap);
        }
    }
}
