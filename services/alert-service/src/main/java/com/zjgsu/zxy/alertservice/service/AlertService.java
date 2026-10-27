package com.zjgsu.zxy.alertservice.service;

import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final RestTemplate restTemplate;

    @Value("${vehicle.service.url}")
    private String vehicleServiceUrl;

    public AlertService(AlertRepository alertRepository, RestTemplate restTemplate) {
        this.alertRepository = alertRepository;
        this.restTemplate = restTemplate;
    }

    public Alert createAlert(Alert alert) {
        // 1. 本地业务规则校验
        if (alert.getLevel() == null || alert.getLevel() < 1 || alert.getLevel() > 3) {
            throw new IllegalArgumentException("告警等级不合法，必须在 1-3 之间");
        }

        // 2. 跨服务校验：打电话给车辆服务
        try {
            String url = vehicleServiceUrl + "/" + alert.getVehicleId();
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            // 车辆不存在，拒绝写入
            throw new IllegalArgumentException("车辆ID " + alert.getVehicleId() + " 不存在，无法创建告警");
        } catch (Exception e) {
            // 车辆服务挂了
            throw new RuntimeException("车辆服务不可用，请稍后再试: " + e.getMessage());
        }

        // 3. 校验通过，写入数据库
        alert.setStatus("pending");
        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsByVehicle(Long vehicleId) {
        return alertRepository.findByVehicleId(vehicleId);
    }
}