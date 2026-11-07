package com.zjgsu.zxy.carbonservice.service;

import com.zjgsu.zxy.carbonservice.config.CarbonFactorProperties;
import com.zjgsu.zxy.carbonservice.dto.CarbonReportRequest;
import com.zjgsu.zxy.carbonservice.dto.CarbonReportResponse;
import com.zjgsu.zxy.carbonservice.dto.DrivingReportSnapshot;
import com.zjgsu.zxy.carbonservice.entity.CarbonReport;
import com.zjgsu.zxy.carbonservice.exception.ResourceNotFoundException;
import com.zjgsu.zxy.carbonservice.exception.ServiceUnavailableException;
import com.zjgsu.zxy.carbonservice.repository.CarbonReportRepository;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class CarbonReportService {

    private final CarbonReportRepository repository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final CarbonFactorProperties factors;

    public CarbonReportService(
            CarbonReportRepository repository,
            RestTemplate restTemplate,
            DiscoveryClient discoveryClient,
            CarbonFactorProperties factors) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
        this.factors = factors;
    }

    public CarbonReportResponse generateReport(CarbonReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("碳排报告请求不能为空");
        }
        validateVehicleId(request.getVehicleId());
        String month = parseMonth(request.getMonth()).toString();
        DrivingReportSnapshot driving = fetchDrivingReport(request.getVehicleId(), month);

        BigDecimal co2Kg = driving.totalFuelLiters()
                .multiply(factors.getGasolineKgPerLiter())
                .add(driving.totalEnergyKwh().multiply(factors.getGridKgPerKwh()))
                .setScale(3, RoundingMode.HALF_UP);

        CarbonReport report = repository.findByVehicleIdAndMonth(
                        request.getVehicleId(), month)
                .orElseGet(CarbonReport::new);
        report.setVehicleId(request.getVehicleId());
        report.setMonth(month);
        report.setTotalDistanceKm(driving.totalDistanceKm());
        report.setFuelLiters(driving.totalFuelLiters());
        report.setEnergyKwh(driving.totalEnergyKwh());
        report.setCo2Kg(co2Kg);
        report.setCalculatedAt(LocalDateTime.now());

        return toResponse(repository.save(report));
    }

    public List<CarbonReportResponse> getReports(Long vehicleId) {
        validateVehicleId(vehicleId);
        return repository.findByVehicleIdOrderByMonthDesc(vehicleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CarbonReportResponse getReport(Long vehicleId, String month) {
        validateVehicleId(vehicleId);
        String parsedMonth = parseMonth(month).toString();
        CarbonReport report = repository.findByVehicleIdAndMonth(vehicleId, parsedMonth)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "车辆 " + vehicleId + " 在 " + parsedMonth + " 没有碳排报告"));
        return toResponse(report);
    }

    private DrivingReportSnapshot fetchDrivingReport(Long vehicleId, String month) {
        List<ServiceInstance> instances =
                discoveryClient.getInstances("driving-behavior-service");
        if (instances == null || instances.isEmpty()) {
            throw new ServiceUnavailableException(
                    "找不到 driving-behavior-service 实例");
        }

        String url = instances.get(0).getUri()
                + "/api/driving-reports/vehicle/" + vehicleId
                + "/monthly?month=" + month;
        try {
            DrivingReportSnapshot report =
                    restTemplate.getForObject(url, DrivingReportSnapshot.class);
            if (report == null) {
                throw new ServiceUnavailableException("驾驶行为服务没有返回报告");
            }
            return report;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new IllegalArgumentException(
                    "车辆 " + vehicleId + " 在 " + month + " 没有驾驶数据");
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "driving-behavior-service 暂时不可用: " + exception.getMessage());
        }
    }

    private void validateVehicleId(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new IllegalArgumentException("vehicleId 必须是正整数");
        }
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException("month 必须使用 YYYY-MM 格式");
        }
    }

    private CarbonReportResponse toResponse(CarbonReport report) {
        return new CarbonReportResponse(
                report.getId(),
                report.getVehicleId(),
                report.getMonth(),
                report.getTotalDistanceKm(),
                report.getFuelLiters(),
                report.getEnergyKwh(),
                report.getCo2Kg(),
                report.getCalculatedAt());
    }
}
