package com.zjgsu.zxy.drivingbehavior.dto;

import java.math.BigDecimal;

public record DrivingReportResponse(
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
