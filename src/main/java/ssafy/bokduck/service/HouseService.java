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
import java.util.stream.Collectors;
import java.util.Collections;

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
        
        // 시/군/구 이름으로 검색하는 경우, 해당 시/군/구의 모든 읍/면/동 이름을 가져와서 필터링
        if (params.containsKey("gugunName") && params.get("gugunName") != null && !params.get("gugunName").toString().isEmpty()) {
            String gugunName = params.get("gugunName").toString();
            List<DongCodeDto> dongs = houseMapper.getDongsByGugun(gugunName);
            
            // 동 이름 목록 추출 (null이 아니고 빈 문자열이 아닌 것만)
            List<String> dongNames = dongs.stream()
                    .map(DongCodeDto::getDongName)
                    .filter(name -> name != null && !name.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            
            // gugunName 파라미터 제거하고 umdNmList로 대체
            params.remove("gugunName");
            if (!dongNames.isEmpty()) {
                params.put("umdNmList", dongNames);
            } else {
                // 해당 시/군/구에 읍/면/동이 없으면 빈 결과 반환
                return Collections.emptyList();
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

    public List<String> getGugunNames() {
        return houseMapper.getGugunNames();
    }

    public List<DongCodeDto> getDongsByGugun(String gugunName) {
        return houseMapper.getDongsByGugun(gugunName);
    }

    public int countHouseInfos(Map<String, Object> params) {
        // 시/군/구 이름으로 검색하는 경우, 해당 시/군/구의 모든 읍/면/동 이름을 가져와서 필터링
        if (params.containsKey("gugunName") && params.get("gugunName") != null && !params.get("gugunName").toString().isEmpty()) {
            String gugunName = params.get("gugunName").toString();
            List<DongCodeDto> dongs = houseMapper.getDongsByGugun(gugunName);
            
            // 동 이름 목록 추출 (null이 아니고 빈 문자열이 아닌 것만)
            List<String> dongNames = dongs.stream()
                    .map(DongCodeDto::getDongName)
                    .filter(name -> name != null && !name.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            
            // gugunName 파라미터 제거하고 umdNmList로 대체
            params.remove("gugunName");
            if (!dongNames.isEmpty()) {
                params.put("umdNmList", dongNames);
            } else {
                // 해당 시/군/구에 읍/면/동이 없으면 0 반환
                return 0;
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
