package com.zjgsu.zxy.controller;

import com.zjgsu.zxy.dto.VehicleRequest;
import com.zjgsu.zxy.entity.Vehicle;
import com.zjgsu.zxy.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // 接口1：创建车辆
    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody VehicleRequest req) {
        Vehicle v = new Vehicle();
        v.setPlateNumber(req.getPlateNumber());
        v.setBrand(req.getBrand());
        v.setTier(req.getTier());
        return ResponseEntity.ok(vehicleRepository.save(v));
    }

    // 接口2：查询车辆
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}