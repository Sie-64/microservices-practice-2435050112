package com.zjgsu.zxy.carbonservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CarbonReportResponse(
        Long id,
        Long vehicleId,
        String month,
        BigDecimal totalDistanceKm,
        BigDecimal fuelLiters,
        BigDecimal energyKwh,
        BigDecimal co2Kg,
        LocalDateTime calculatedAt) {
}
