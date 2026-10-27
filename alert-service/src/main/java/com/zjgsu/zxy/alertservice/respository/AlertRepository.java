package com.zjgsu.zxy.alertservice.repository;

import com.zjgsu.zxy.alertservice.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByVehicleId(Long vehicleId);
}