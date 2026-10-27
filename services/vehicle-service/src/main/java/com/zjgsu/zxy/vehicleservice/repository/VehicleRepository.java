package com.zjgsu.zxy.vehicleservice.repository;
import com.zjgsu.zxy.vehicleservice.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {}