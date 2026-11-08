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
    private final DiscoveryClient discoveryClient;

    public AlertService(AlertRepository alertRepository, RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.alertRepository = alertRepository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public Alert createAlert(Alert alert) {
        // 1. 本地业务规则校验
        if (alert.getLevel() == null || alert.getLevel() < 1 || alert.getLevel() > 3) {
            throw new IllegalArgumentException("告警等级不合法，必须在 1-3 之间");
        }

        // 2. 每次实时从 Nacos 获取实例列表（老师要求：不能只在启动时读一次）
        List<ServiceInstance> instances = discoveryClient.getInstances("vehicle-service");

        // 3. 实例列表为空，返回 503
        if (instances == null || instances.isEmpty()) {
            throw new ServiceUnavailableException("依赖不可用：找不到 vehicle-service 实例");
        }

        // 4. 取第一个实例拼接 URL
        ServiceInstance instance = instances.get(0);
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/api/vehicles/" + alert.getVehicleId();

        try {
            // 发送 HTTP 请求（RestTemplate 已配置 3 秒超时）
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            // 5. 保留原有 404 业务响应（车辆不存在）
            throw new IllegalArgumentException("车辆ID " + alert.getVehicleId() + " 不存在，无法创建告警");
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("vehicle-service 暂时不可用: " + e.getMessage());
        }

        // 6. 校验通过，保存数据
        alert.setStatus("pending");
        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsByVehicle(Long vehicleId) {
        return alertRepository.findByVehicleId(vehicleId);
    }
}
