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
import java.util.ArrayList;
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
            List<HouseInfoDto> listings = new ArrayList<>();
            Map<String, Object> params = new HashMap<>();
            location = location.trim();

            // 1. Initial Attempt: Check if it's a specific 'Gu' search
            if (location.endsWith("구")) {
                List<DongCodeDto> dongs = houseMapper.searchDongCodes(location);
                if (dongs != null && !dongs.isEmpty()) {
                    // Try to match the exact Gu name
                    String sggCd = dongs.get(0).getDongCode().substring(0, 5);
                    for (DongCodeDto d : dongs) {
                        if (d.getGugunName() != null && d.getGugunName().contains(location.replace(" ", ""))) {
                            sggCd = d.getDongCode().substring(0, 5);
                            break;
                        }
                    }
                    params.put("sggCd", sggCd);
                    params.put("limit", limit);
                    listings = houseMapper.searchHouseInfos(params);
                }
            }

            // 2. If listings empty (not Gu or Gu failed), Broad Search
            if (listings.isEmpty()) {
                List<DongCodeDto> codeMatches = houseMapper.searchDongCodes(location);

                if (codeMatches != null && !codeMatches.isEmpty()) {
                    // Collect distinct Dong Names
                    List<String> umdNmList = new ArrayList<>();
                    for (DongCodeDto dto : codeMatches) {
                        // Logic: if search term is "Sido" or "Gugun", we want all dongs in it.
                        if (dto.getDongName() != null && !dto.getDongName().isEmpty()) {
                            if (!umdNmList.contains(dto.getDongName())) {
                                umdNmList.add(dto.getDongName());
                            }
                        }
                        if (umdNmList.size() >= 50)
                            break; // Limit to 50 dongs
                    }

                    if (!umdNmList.isEmpty()) {
                        params.clear();
                        params.put("umdNmList", umdNmList);
                        params.put("limit", limit);
                        listings = houseMapper.searchHouseInfos(params);
                    }
                }
            }

            // 3. Last Resort: Direct Matches (in case searchDongCodes partial match failed)
            if (listings.isEmpty()) {
                params.clear();
                params.put("umdNm", location);
                params.put("limit", limit);
                listings = houseMapper.searchHouseInfos(params);
            }

            // 4. Store in Context & Debug
            searchContext.clear();
            searchContext.addListings(listings);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(listings);

            if (listings.isEmpty()) {
                StringBuilder debug = new StringBuilder();
                debug.append("DEBUG INFO: Location='").append(location).append("'");
                debug.append(", Strategy='Multi-Layer Search'");
                debug.append(", listings=0");
                return debug.toString();
            }
            return json;

        } catch (Exception e) {
            e.printStackTrace();
            return "DEBUG INFO: Error=" + e.getMessage();
        }
    }
}
