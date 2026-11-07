package com.zjgsu.zxy.drivingbehavior.service;

import com.zjgsu.zxy.drivingbehavior.dto.DrivingEventRequest;
import com.zjgsu.zxy.drivingbehavior.dto.DrivingReportResponse;
import com.zjgsu.zxy.drivingbehavior.entity.DrivingEvent;
import com.zjgsu.zxy.drivingbehavior.exception.ResourceNotFoundException;
import com.zjgsu.zxy.drivingbehavior.exception.ServiceUnavailableException;
import com.zjgsu.zxy.drivingbehavior.repository.DrivingEventRepository;
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
public class DrivingBehaviorService {

    private static final BigDecimal HIGH_SPEED_THRESHOLD = new BigDecimal("120");

    private final DrivingEventRepository repository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public DrivingBehaviorService(
            DrivingEventRepository repository,
            RestTemplate restTemplate,
            DiscoveryClient discoveryClient) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public DrivingEvent createEvent(DrivingEventRequest request) {
        validateRequest(request);
        verifyVehicleExists(request.getVehicleId());

        DrivingEvent event = new DrivingEvent();
        event.setVehicleId(request.getVehicleId());
        event.setOccurredAt(request.getOccurredAt());
        event.setDistanceKm(request.getDistanceKm());
        event.setFuelLiters(zeroIfNull(request.getFuelLiters()));
        event.setEnergyKwh(zeroIfNull(request.getEnergyKwh()));
        event.setHarshAccelerationCount(zeroIfNull(request.getHarshAccelerationCount()));
        event.setHarshBrakingCount(zeroIfNull(request.getHarshBrakingCount()));
        event.setMaxSpeedKph(zeroIfNull(request.getMaxSpeedKph()));
        event.setCreatedAt(LocalDateTime.now());
        return repository.save(event);
    }

    public List<DrivingEvent> getEvents(Long vehicleId) {
        validateVehicleId(vehicleId);
        return repository.findByVehicleIdOrderByOccurredAtDesc(vehicleId);
    }

    public DrivingReportResponse buildMonthlyReport(Long vehicleId, String month) {
        validateVehicleId(vehicleId);
        YearMonth yearMonth = parseMonth(month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        List<DrivingEvent> events =
                repository.findByVehicleIdAndOccurredAtBetween(vehicleId, start, end);

        if (events.isEmpty()) {
            throw new ResourceNotFoundException(
                    "车辆 " + vehicleId + " 在 " + yearMonth + " 没有驾驶数据");
        }

        BigDecimal distance = sum(events, DrivingEvent::getDistanceKm);
        BigDecimal fuel = sum(events, DrivingEvent::getFuelLiters);
        BigDecimal energy = sum(events, DrivingEvent::getEnergyKwh);
        int harshAcceleration = events.stream()
                .mapToInt(event -> event.getHarshAccelerationCount())
                .sum();
        int harshBraking = events.stream()
                .mapToInt(event -> event.getHarshBrakingCount())
                .sum();
        int highSpeedEvents = (int) events.stream()
                .filter(event -> event.getMaxSpeedKph().compareTo(HIGH_SPEED_THRESHOLD) > 0)
                .count();

        int score = Math.max(
                0,
                100
                        - Math.min(30, harshAcceleration * 2)
                        - Math.min(30, harshBraking * 3)
                        - Math.min(20, highSpeedEvents * 2));

        return new DrivingReportResponse(
                vehicleId,
                yearMonth.toString(),
                events.size(),
                scale(distance),
                scale(fuel),
                scale(energy),
                harshAcceleration,
                harshBraking,
                highSpeedEvents,
                score);
    }

    private void verifyVehicleExists(Long vehicleId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("vehicle-service");
        if (instances == null || instances.isEmpty()) {
            throw new ServiceUnavailableException("找不到 vehicle-service 实例");
        }

        String url = instances.get(0).getUri() + "/api/vehicles/" + vehicleId;
        try {
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new IllegalArgumentException("车辆ID " + vehicleId + " 不存在");
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "vehicle-service 暂时不可用: " + exception.getMessage());
        }
    }

    private void validateRequest(DrivingEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("驾驶事件不能为空");
        }
        validateVehicleId(request.getVehicleId());
        if (request.getOccurredAt() == null) {
            throw new IllegalArgumentException("occurredAt 不能为空");
        }
        if (request.getDistanceKm() == null
                || request.getDistanceKm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("distanceKm 必须大于 0");
        }
        requireNonNegative(request.getFuelLiters(), "fuelLiters");
        requireNonNegative(request.getEnergyKwh(), "energyKwh");
        requireNonNegative(request.getMaxSpeedKph(), "maxSpeedKph");
        requireNonNegative(request.getHarshAccelerationCount(), "harshAccelerationCount");
        requireNonNegative(request.getHarshBrakingCount(), "harshBrakingCount");
    }

    private void validateVehicleId(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new IllegalArgumentException("vehicleId 必须是正整数");
        }
    }

    private void requireNonNegative(BigDecimal value, String field) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " 不能为负数");
        }
    }

    private void requireNonNegative(Integer value, String field) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(field + " 不能为负数");
        }
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException("month 必须使用 YYYY-MM 格式");
        }
    }

    private BigDecimal sum(
            List<DrivingEvent> events,
            java.util.function.Function<DrivingEvent, BigDecimal> extractor) {
        return events.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
