package com.zjgsu.zxy.alertservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;

@SpringBootTest
class AlertServiceApplicationTests {
	@MockBean
	private DiscoveryClient discoveryClient;

	@Test
	void contextLoads() {
	}
}
