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
        return searchProperties(location, 10); // Default limit 10
    }

    public String searchProperties(String location, int limit) {
        try {
            // 1. Find Dong Code
            List<DongCodeDto> dongs = houseMapper.searchDongCodes(location);
            if (dongs == null || dongs.isEmpty()) {
                return "죄송합니다. '" + location + "' 지역을 찾을 수 없습니다.";
            }

            // 2. Determine Search Type
            Map<String, Object> params = new HashMap<>();
            DongCodeDto first = dongs.get(0);
            String sggCd = first.getDongCode().substring(0, 5);

            // Logic for Gu vs Dong
            boolean matchGu = false;
            if (location.endsWith("구")) {
                for (DongCodeDto d : dongs) {
                    if (d.getGugunName() != null && d.getGugunName().contains(location.replace(" ", ""))) {
                        matchGu = true;
                        sggCd = d.getDongCode().substring(0, 5);
                        break;
                    }
                }
            }

            if (matchGu) {
                params.put("sggCd", sggCd);
            } else {
                params.put("umdNm", first.getDongName());
            }
            params.put("limit", limit);

            List<HouseInfoDto> listings = houseMapper.searchHouseInfos(params);

            // 3. Store in Context
            searchContext.clear();
            searchContext.addListings(listings);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(listings);

            // DEBUG: If empty, include debug info in the return string so LLM mentions it
            if (listings.isEmpty()) {
                StringBuilder debug = new StringBuilder();
                debug.append("DEBUG INFO: Listings=0");
                debug.append(", P=").append(params);
                debug.append(", Dongs=").append(dongs.size());
                if (!dongs.isEmpty())
                    debug.append(", 1st=").append(dongs.get(0).getDongName());
                return debug.toString();
            }
            return json;

        } catch (Exception e) {
            return "DEBUG INFO: Error=" + e.getMessage();
        }
    }
}
