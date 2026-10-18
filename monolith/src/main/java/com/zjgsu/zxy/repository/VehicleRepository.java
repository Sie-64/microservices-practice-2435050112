package com.zjgsu.zxy.repository;

import com.zjgsu.zxy.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}