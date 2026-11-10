package com.zjgsu.zxy.alertservice.client;

import org.springframework.stereotype.Component;

@Component
public class VehicleClientFallback implements VehicleClient {
    @Override
    public Object getVehicleById(Long id) {
        // 当车辆服务不可用时，抛出此异常
        throw new RuntimeException("车辆服务不可用，已触发降级：无法校验车辆信息");
    }
}