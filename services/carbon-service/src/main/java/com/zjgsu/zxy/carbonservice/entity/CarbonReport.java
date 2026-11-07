package com.zjgsu.zxy.carbonservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "carbon_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "report_month"}))
public class CarbonReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "report_month", nullable = false, length = 7)
    private String month;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal totalDistanceKm;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal fuelLiters;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal energyKwh;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal co2Kg;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(BigDecimal totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
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

    public BigDecimal getCo2Kg() {
        return co2Kg;
    }

    public void setCo2Kg(BigDecimal co2Kg) {
        this.co2Kg = co2Kg;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
