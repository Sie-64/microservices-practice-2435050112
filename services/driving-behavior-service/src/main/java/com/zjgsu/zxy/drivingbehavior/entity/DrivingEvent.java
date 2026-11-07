package com.zjgsu.zxy.drivingbehavior.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driving_events")
public class DrivingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal distanceKm;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal fuelLiters;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal energyKwh;

    @Column(nullable = false)
    private Integer harshAccelerationCount;

    @Column(nullable = false)
    private Integer harshBrakingCount;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal maxSpeedKph;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
