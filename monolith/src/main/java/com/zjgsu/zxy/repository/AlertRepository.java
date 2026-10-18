package com.zjgsu.zxy.repository;

import com.zjgsu.zxy.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    // 按车辆ID查询告警
    List<Alert> findByVehicleId(Long vehicleId);
}