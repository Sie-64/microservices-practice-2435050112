package com.zjgsu.zxy.carbonservice.service;

import com.zjgsu.zxy.carbonservice.config.CarbonFactorProperties;
import com.zjgsu.zxy.carbonservice.dto.CarbonReportRequest;
import com.zjgsu.zxy.carbonservice.dto.CarbonReportResponse;
import com.zjgsu.zxy.carbonservice.dto.DrivingReportSnapshot;
import com.zjgsu.zxy.carbonservice.entity.CarbonReport;
import com.zjgsu.zxy.carbonservice.exception.ServiceUnavailableException;
import com.zjgsu.zxy.carbonservice.repository.CarbonReportRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CarbonReportServiceTest {

    @Mock
    private CarbonReportRepository repository;

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private ServiceInstance serviceInstance;

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private CarbonReportService service;

    @BeforeEach
    void setUp() {
        CarbonFactorProperties factors = new CarbonFactorProperties();
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new CarbonReportService(
                repository, restTemplate, discoveryClient, factors);
    }

    @Test
    void calculatesAndPersistsCarbonReport() {
        when(discoveryClient.getInstances("driving-behavior-service"))
                .thenReturn(List.of(serviceInstance));
        when(serviceInstance.getUri()).thenReturn(URI.create("http://driving:8084"));
        when(repository.findByVehicleIdAndMonth(1L, "2026-11"))
                .thenReturn(Optional.empty());
        when(repository.save(any(CarbonReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        server.expect(requestTo(
                        "http://driving:8084/api/driving-reports/vehicle/1/monthly?month=2026-11"))
                .andRespond(withSuccess("""
                        {
                          "vehicleId": 1,
                          "month": "2026-11",
                          "tripCount": 2,
                          "totalDistanceKm": 100.000,
                          "totalFuelLiters": 10.000,
                          "totalEnergyKwh": 20.000,
                          "harshAccelerationCount": 1,
                          "harshBrakingCount": 1,
                          "highSpeedEventCount": 0,
                          "score": 94
                        }
                        """, MediaType.APPLICATION_JSON));

        CarbonReportResponse response = service.generateReport(request());

        server.verify();
        assertThat(response.co2Kg()).isEqualByComparingTo("34.720");
        assertThat(response.totalDistanceKm()).isEqualByComparingTo("100.000");
    }

    @Test
    void returnsServiceUnavailableWhenDrivingServiceHasNoInstance() {
        when(discoveryClient.getInstances("driving-behavior-service"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.generateReport(request()))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("driving-behavior-service");
    }

    private CarbonReportRequest request() {
        CarbonReportRequest request = new CarbonReportRequest();
        request.setVehicleId(1L);
        request.setMonth("2026-11");
        return request;
    }
}
