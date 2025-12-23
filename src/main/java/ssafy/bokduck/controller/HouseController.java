package ssafy.bokduck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.bokduck.dto.HouseDealDto;
import ssafy.bokduck.dto.HouseInfoDto;
import ssafy.bokduck.dto.DongCodeDto;
import ssafy.bokduck.service.HouseService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    public ResponseEntity<List<HouseInfoDto>> searchHouses(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(houseService.searchHouseInfos(params));
    }

    @GetMapping("/{aptSeq}")
    public ResponseEntity<HouseInfoDto> getHouseDetail(@PathVariable String aptSeq) {
        return ResponseEntity.ok(houseService.getHouseDetail(aptSeq));
    }

    @GetMapping("/{aptSeq}/deals")
    public ResponseEntity<List<HouseDealDto>> getHouseDeals(@PathVariable String aptSeq) {
        return ResponseEntity.ok(houseService.getHouseDeals(aptSeq));
    }

    @GetMapping("/dong")
    public ResponseEntity<List<DongCodeDto>> searchDongCodes(@RequestParam String dongName) {
        return ResponseEntity.ok(houseService.searchDongCodes(dongName));
    }

    @GetMapping("/gugun")
    public ResponseEntity<List<String>> getGugunNames() {
        return ResponseEntity.ok(houseService.getGugunNames());
    }

    @GetMapping("/dong/gugun/{gugunName}")
    public ResponseEntity<List<DongCodeDto>> getDongsByGugun(@PathVariable String gugunName) {
        return ResponseEntity.ok(houseService.getDongsByGugun(gugunName));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countHouses(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(houseService.countHouseInfos(params));
    }

    @PostMapping("/{aptSeq}/scrap")
    public ResponseEntity<?> toggleScrap(@PathVariable String aptSeq, @RequestParam Long userId) {
        // In real app, userId comes from Token. For development, we accept it as param.
        houseService.toggleScrap(userId, aptSeq);
        return ResponseEntity.ok("Scrap toggled");
    }

    @GetMapping("/scraps")
    public ResponseEntity<List<HouseInfoDto>> getScrappedHouses(@RequestParam Long userId) {
        return ResponseEntity.ok(houseService.getScrappedHouses(userId));
    }
}
