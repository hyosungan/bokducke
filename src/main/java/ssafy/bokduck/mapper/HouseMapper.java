package ssafy.bokduck.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ssafy.bokduck.dto.HouseInfoDto;
import ssafy.bokduck.dto.HouseDealDto;
import ssafy.bokduck.dto.DongCodeDto;
import ssafy.bokduck.dto.PropertyScrapDto;
import java.util.List;
import java.util.Map;

@Mapper
public interface HouseMapper {
    // House Info
    HouseInfoDto findHouseInfoByAptSeq(String aptSeq);

    List<HouseInfoDto> searchHouseInfos(Map<String, Object> params); // dongCode, aptName
    int countHouseInfos(Map<String, Object> params); // 매물 개수 조회

    // House Deals
    List<HouseDealDto> findDealsByAptSeq(String aptSeq);

    // Dong Codes
    List<DongCodeDto> searchDongCodes(String dongName);
    List<String> getSidoNames(); // 시/도 목록 조회
    List<String> getGugunNames(@Param("sidoName") String sidoName); // 시/군/구 목록 조회 (시/도로 필터링 가능)
    List<DongCodeDto> getDongsByGugun(String gugunName); // 특정 시/군/구의 읍/면/동 목록 조회 (하위 호환성)
    List<DongCodeDto> getDongsBySidoAndGugun(@Param("sidoName") String sidoName, @Param("gugunName") String gugunName); // 특정 시/도와 시/군/구의 읍/면/동 목록 조회
    List<DongCodeDto> getDongCodesBySido(@Param("sidoName") String sidoName); // 특정 시/도의 모든 dong_code 목록 조회

    // Scraps
    void insertScrap(PropertyScrapDto scrap);

    void deleteScrap(@Param("userId") Long userId, @Param("aptSeq") String aptSeq);

    int checkScrap(@Param("userId") Long userId, @Param("aptSeq") String aptSeq);

    List<HouseInfoDto> findScrappedHouses(Long userId);
}
