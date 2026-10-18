package com.zjgsu.zxy.dto;

public class AlertRequest {
    private Long vehicleId;
    private String alertType;
    private Integer level;

    // Getters and Setters
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}