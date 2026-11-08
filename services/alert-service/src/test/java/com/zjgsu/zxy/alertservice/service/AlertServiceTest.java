package com.zjgsu.zxy.alertservice.service;

import com.zjgsu.zxy.alertservice.entity.Alert;
import com.zjgsu.zxy.alertservice.exception.ServiceUnavailableException;
import com.zjgsu.zxy.alertservice.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertServiceTest {

    @Test
    void rejectsCreationWhenVehicleServiceHasNoInstance() {
        AlertRepository repository = mock(AlertRepository.class);
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getInstances("vehicle-service")).thenReturn(List.of());
        AlertService service =
                new AlertService(repository, new RestTemplate(), discoveryClient);
        Alert alert = new Alert();
        alert.setVehicleId(1L);
        alert.setAlertType("battery");
        alert.setLevel(1);

        assertThatThrownBy(() -> service.createAlert(alert))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("vehicle-service");
    }
}
