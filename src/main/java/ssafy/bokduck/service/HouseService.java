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
