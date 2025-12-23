package ssafy.bokduck.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import ssafy.bokduck.context.PropertySearchContext;
import ssafy.bokduck.dto.DongCodeDto;
import ssafy.bokduck.dto.HouseInfoDto;
import ssafy.bokduck.mapper.HouseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RealEstateTools {

    private final HouseMapper houseMapper;
    private final PropertySearchContext searchContext;

    public RealEstateTools(HouseMapper houseMapper, PropertySearchContext searchContext) {
        this.houseMapper = houseMapper;
        this.searchContext = searchContext;
    }

    @Tool(description = "위치를 물어보면 그거에 해당하는 매물들을 DB에서 검색해서 보여주는 모델")
    public String searchProperties(String location) {
        // 1. Find Dong Code
        List<DongCodeDto> dongs = houseMapper.searchDongCodes(location);
        if (dongs == null || dongs.isEmpty()) {
            return "죄송합니다. '" + location + "' 지역을 찾을 수 없습니다.";
        }

        // For simplicity, use the first matching dong
        String dongCode = dongs.get(0).getDongCode();
        String dongName = dongs.get(0).getDongName();

        // 2. Search Houses
        Map<String, Object> params = new HashMap<>();
        params.put("dongCode", dongCode);

        List<HouseInfoDto> listings = houseMapper.searchHouseInfos(params);

        // 3. Store in Context
        searchContext.clear();
        searchContext.addListings(listings);

        // 4. Return JSON String to LLM
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(listings);
        } catch (Exception e) {
            return "[]";
        }
    }
}
