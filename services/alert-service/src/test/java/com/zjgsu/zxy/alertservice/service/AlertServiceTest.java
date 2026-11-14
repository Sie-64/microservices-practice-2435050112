package com.zjgsu.zxy.alertservice.service;

import com.zjgsu.zxy.alertservice.client.VehicleClient;
import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AlertServiceTest {

    @Autowired
    private AlertService alertService;

    @MockBean
    private AlertRepository alertRepository;

    // 🎯 模拟 Feign 客户端
    @MockBean
    private VehicleClient vehicleClient;

    @Test
    void testCreateAlert_DependencyUnavailable_ShouldThrowException() {
        // 模拟车辆服务不可用，Feign 调用时抛出异常（相当于触发了 Fallback）
        when(vehicleClient.getVehicleById(any())).thenThrow(new RuntimeException("车辆服务不可用，已触发降级：无法校验车辆信息"));

        Alert alert = new Alert();
        alert.setVehicleId(1L);
        alert.setAlertType("碰撞");
        alert.setLevel(1);

        // 验证是否抛出了业务异常，证明阻断生效，没有乱写数据库
        assertThrows(IllegalArgumentException.class, () -> {
            alertService.createAlert(alert);
        });
    }
}