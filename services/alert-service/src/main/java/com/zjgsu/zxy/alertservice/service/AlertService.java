package com.zjgsu.zxy.alertservice.service;

import com.zjgsu.zxy.alertservice.client.VehicleClient;
import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final VehicleClient vehicleClient; // 注入 Feign 客户端

    public AlertService(AlertRepository alertRepository, VehicleClient vehicleClient) {
        this.alertRepository = alertRepository;
        this.vehicleClient = vehicleClient;
    }

    public Alert createAlert(Alert alert) {
        // 1. 本地业务规则校验
        if (alert.getLevel() == null || alert.getLevel() < 1 || alert.getLevel() > 3) {
            throw new IllegalArgumentException("告警等级不合法，必须在 1-3 之间");
        }

        // 2. 核心改造：像调用本地方法一样调用远程服务
        try {
            vehicleClient.getVehicleById(alert.getVehicleId());
        } catch (Exception e) {
            // 捕获抛出的降级异常
            throw new IllegalArgumentException(e.getMessage());
        }

        // 3. 校验通过，保存数据
        alert.setStatus("pending");
        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsByVehicle(Long vehicleId) {
        return alertRepository.findByVehicleId(vehicleId);
    }
}