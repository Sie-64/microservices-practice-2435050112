package com.zjgsu.zxy.drivingbehavior.repository;

import com.zjgsu.zxy.drivingbehavior.entity.DrivingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DrivingEventRepository extends JpaRepository<DrivingEvent, Long> {

    List<DrivingEvent> findByVehicleIdOrderByOccurredAtDesc(Long vehicleId);

    List<DrivingEvent> findByVehicleIdAndOccurredAtBetween(
            Long vehicleId, LocalDateTime start, LocalDateTime end);
}
