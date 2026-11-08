package com.zjgsu.zxy.alertservice.service;

import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.exception.ServiceUnavailableException;
import com.zjgsu.zxy.alertservice.repository.AlertRepository;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient; // 🎯 注入发现客户端

    public AlertService(AlertRepository alertRepository, RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.alertRepository = alertRepository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public Alert createAlert(Alert alert) {
        if (alert.getLevel() == null || alert.getLevel() < 1 || alert.getLevel() > 3) {
            throw new IllegalArgumentException("告警等级不合法，必须在 1-3 之间");
        }

        // 🎯 核心改造：从 Nacos 获取 vehicle-service 的实例列表
        List<ServiceInstance> instances = discoveryClient.getInstances("vehicle-service");
        if (instances == null || instances.isEmpty()) {
            throw new ServiceUnavailableException("找不到 vehicle-service 实例");
        }

        // 取第一个实例的 IP 和 端口
        ServiceInstance instance = instances.get(0);
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/api/vehicles/" + alert.getVehicleId();

        try {
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("车辆ID " + alert.getVehicleId() + " 不存在，无法创建告警");
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("vehicle-service 暂时不可用: " + e.getMessage());
        }

        alert.setStatus("pending");
        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsByVehicle(Long vehicleId) {
        return alertRepository.findByVehicleId(vehicleId);
    }
}
