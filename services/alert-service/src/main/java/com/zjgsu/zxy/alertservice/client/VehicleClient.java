package com.zjgsu.zxy.alertservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 声明调用 vehicle-service，并指定降级类
@FeignClient(name = "vehicle-service", fallback = VehicleClientFallback.class)
public interface VehicleClient {
    @GetMapping("/api/vehicles/{id}")
    Object getVehicleById(@PathVariable("id") Long id);
}