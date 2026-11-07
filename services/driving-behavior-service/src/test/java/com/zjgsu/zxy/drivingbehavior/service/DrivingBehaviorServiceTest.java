package com.zjgsu.zxy.drivingbehavior.service;

import com.zjgsu.zxy.drivingbehavior.dto.DrivingEventRequest;
import com.zjgsu.zxy.drivingbehavior.dto.DrivingReportResponse;
import com.zjgsu.zxy.drivingbehavior.entity.DrivingEvent;
import com.zjgsu.zxy.drivingbehavior.exception.ServiceUnavailableException;
import com.zjgsu.zxy.drivingbehavior.repository.DrivingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class DrivingBehaviorServiceTest {

    @Mock
    private DrivingEventRepository repository;

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private ServiceInstance serviceInstance;

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private DrivingBehaviorService service;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new DrivingBehaviorService(repository, restTemplate, discoveryClient);
    }

    @Test
    void createsEventAfterVehicleDiscovery() {
        when(discoveryClient.getInstances("vehicle-service"))
                .thenReturn(List.of(serviceInstance));
        when(serviceInstance.getUri()).thenReturn(URI.create("http://vehicle-service:8082"));
        when(repository.save(any(DrivingEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        server.expect(requestTo("http://vehicle-service:8082/api/vehicles/1"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        DrivingEvent event = service.createEvent(request());

        server.verify();
        assertThat(event.getVehicleId()).isEqualTo(1L);
        assertThat(event.getDistanceKm()).isEqualByComparingTo("12.5");
        assertThat(event.getFuelLiters()).isEqualByComparingTo("1.2");
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    void returnsServiceUnavailableWhenNoVehicleInstanceExists() {
        when(discoveryClient.getInstances("vehicle-service")).thenReturn(List.of());

        assertThatThrownBy(() -> service.createEvent(request()))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("vehicle-service");
    }

    @Test
    void calculatesMonthlyDrivingReport() {
        DrivingEvent first = event("10.0", "1.0", "0", 2, 1, "130");
        DrivingEvent second = event("5.0", "0.5", "0", 1, 1, "90");
        when(repository.findByVehicleIdAndOccurredAtBetween(
                any(), any(), any())).thenReturn(List.of(first, second));

        DrivingReportResponse report = service.buildMonthlyReport(1L, "2026-11");

        assertThat(report.tripCount()).isEqualTo(2);
        assertThat(report.totalDistanceKm()).isEqualByComparingTo("15.000");
        assertThat(report.harshAccelerationCount()).isEqualTo(3);
        assertThat(report.harshBrakingCount()).isEqualTo(2);
        assertThat(report.highSpeedEventCount()).isEqualTo(1);
        assertThat(report.score()).isEqualTo(86);
    }

    private DrivingEventRequest request() {
        DrivingEventRequest request = new DrivingEventRequest();
        request.setVehicleId(1L);
        request.setOccurredAt(LocalDateTime.of(2026, 11, 7, 9, 0));
        request.setDistanceKm(new BigDecimal("12.5"));
        request.setFuelLiters(new BigDecimal("1.2"));
        request.setEnergyKwh(BigDecimal.ZERO);
        request.setHarshAccelerationCount(1);
        request.setHarshBrakingCount(0);
        request.setMaxSpeedKph(new BigDecimal("88"));
        return request;
    }

    private DrivingEvent event(
            String distance,
            String fuel,
            String energy,
            int acceleration,
            int braking,
            String speed) {
        DrivingEvent event = new DrivingEvent();
        event.setVehicleId(1L);
        event.setOccurredAt(LocalDateTime.of(2026, 11, 7, 9, 0));
        event.setDistanceKm(new BigDecimal(distance));
        event.setFuelLiters(new BigDecimal(fuel));
        event.setEnergyKwh(new BigDecimal(energy));
        event.setHarshAccelerationCount(acceleration);
        event.setHarshBrakingCount(braking);
        event.setMaxSpeedKph(new BigDecimal(speed));
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }
}
