package com.zjgsu.zxy.drivingbehavior.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DrivingEventRequest {

    private Long vehicleId;
    private LocalDateTime occurredAt;
    private BigDecimal distanceKm;
    private BigDecimal fuelLiters;
    private BigDecimal energyKwh;
    private Integer harshAccelerationCount;
    private Integer harshBrakingCount;
    private BigDecimal maxSpeedKph;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getFuelLiters() {
        return fuelLiters;
    }

    public void setFuelLiters(BigDecimal fuelLiters) {
        this.fuelLiters = fuelLiters;
    }

    public BigDecimal getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(BigDecimal energyKwh) {
        this.energyKwh = energyKwh;
    }

    public Integer getHarshAccelerationCount() {
        return harshAccelerationCount;
    }

    public void setHarshAccelerationCount(Integer harshAccelerationCount) {
        this.harshAccelerationCount = harshAccelerationCount;
    }

    public Integer getHarshBrakingCount() {
        return harshBrakingCount;
    }

    public void setHarshBrakingCount(Integer harshBrakingCount) {
        this.harshBrakingCount = harshBrakingCount;
    }

    public BigDecimal getMaxSpeedKph() {
        return maxSpeedKph;
    }

    public void setMaxSpeedKph(BigDecimal maxSpeedKph) {
        this.maxSpeedKph = maxSpeedKph;
    }
}
