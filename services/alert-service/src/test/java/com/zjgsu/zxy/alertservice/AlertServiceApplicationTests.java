package com.zjgsu.zxy.alertservice;

import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.exception.ServiceUnavailableException;
import com.zjgsu.zxy.alertservice.repository.AlertRepository;
import com.zjgsu.zxy.alertservice.service.AlertService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AlertServiceApplicationTests {

	@Autowired
	private AlertService alertService;

	// 模拟组件，防止测试连接真实数据库或网络
	@MockBean
	private RestTemplate restTemplate;
	@MockBean
	private AlertRepository alertRepository;
	@MockBean
	private org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient;

	@Test
	void testCreateAlert_NoInstances_ShouldThrow503() {
		// 模拟 Nacos 返回空实例列表
		Mockito.when(discoveryClient.getInstances("vehicle-service")).thenReturn(Collections.emptyList());

		Alert alert = new Alert();
		alert.setVehicleId(1L);
		alert.setAlertType("碰撞");
		alert.setLevel(1);

		// 验证是否抛出了 503 异常
		assertThrows(ServiceUnavailableException.class, () -> {
			alertService.createAlert(alert);
		});
	}
}
