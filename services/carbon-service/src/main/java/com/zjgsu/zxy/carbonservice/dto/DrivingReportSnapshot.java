package com.zjgsu.zxy.carbonservice.dto;

import java.math.BigDecimal;

public record DrivingReportSnapshot(
        Long vehicleId,
        String month,
        int tripCount,
        BigDecimal totalDistanceKm,
        BigDecimal totalFuelLiters,
        BigDecimal totalEnergyKwh,
        int harshAccelerationCount,
        int harshBrakingCount,
        int highSpeedEventCount,
        int score) {
}
