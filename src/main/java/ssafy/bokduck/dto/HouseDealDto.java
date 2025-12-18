package ssafy.bokduck.dto;

import java.math.BigDecimal;

public class HouseDealDto {
    private Long no;
    private String aptSeq;
    private String aptDong;
    private String floor;
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;
    private BigDecimal excluUseAr;
    private String dealAmount;

    public HouseDealDto() {
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public String getAptSeq() {
        return aptSeq;
    }

    public void setAptSeq(String aptSeq) {
        this.aptSeq = aptSeq;
    }

    public String getAptDong() {
        return aptDong;
    }

    public void setAptDong(String aptDong) {
        this.aptDong = aptDong;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Integer getDealYear() {
        return dealYear;
    }

    public void setDealYear(Integer dealYear) {
        this.dealYear = dealYear;
    }

    public Integer getDealMonth() {
        return dealMonth;
    }

    public void setDealMonth(Integer dealMonth) {
        this.dealMonth = dealMonth;
    }

    public Integer getDealDay() {
        return dealDay;
    }

    public void setDealDay(Integer dealDay) {
        this.dealDay = dealDay;
    }

    public BigDecimal getExcluUseAr() {
        return excluUseAr;
    }

    public void setExcluUseAr(BigDecimal excluUseAr) {
        this.excluUseAr = excluUseAr;
    }

    public String getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(String dealAmount) {
        this.dealAmount = dealAmount;
    }
}
