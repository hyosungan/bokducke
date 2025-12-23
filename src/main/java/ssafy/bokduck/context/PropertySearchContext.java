package ssafy.bokduck.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import ssafy.bokduck.dto.HouseInfoDto;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class PropertySearchContext {
    private List<HouseInfoDto> foundListings = new ArrayList<>();

    public void addListings(List<HouseInfoDto> listings) {
        if (listings != null) {
            this.foundListings.addAll(listings);
        }
    }

    public List<HouseInfoDto> getFoundListings() {
        return foundListings;
    }

    public void clear() {
        this.foundListings.clear();
    }
}
