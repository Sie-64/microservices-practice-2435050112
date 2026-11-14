package com.zjgsu.zxy.vehicleservice.controller;

import com.zjgsu.zxy.vehicleservice.entity.Vehicle;
import com.zjgsu.zxy.vehicleservice.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleRepository repository;

    // 🎯 1. 从环境变量读取实例名（在 compose.yaml 里配置的）
    @Value("${INSTANCE_NAME:unknown}")
    private String instanceName;

    public VehicleController(VehicleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Vehicle> getAll() { return repository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        // 🎯 2. 加上这句核心的打印日志
        System.out.println(">>> 车辆服务实例 [" + instanceName + "] 正在处理查询请求...");

        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Vehicle create(@RequestBody Vehicle vehicle) {
        return repository.save(vehicle);
    }
}