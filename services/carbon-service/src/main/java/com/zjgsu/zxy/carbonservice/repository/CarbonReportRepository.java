package com.zjgsu.zxy.carbonservice.repository;

import com.zjgsu.zxy.carbonservice.entity.CarbonReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarbonReportRepository extends JpaRepository<CarbonReport, Long> {

    Optional<CarbonReport> findByVehicleIdAndMonth(Long vehicleId, String month);

    Optional<CarbonReport> findFirstByVehicleIdOrderByMonthDesc(Long vehicleId);

    List<CarbonReport> findByVehicleIdOrderByMonthDesc(Long vehicleId);
}
