package com.zjgsu.zxy.carbonservice.dto;

public class CarbonReportRequest {

    private Long vehicleId;
    private String month;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
